package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.IdentifierType;
import com.stu.edu.vn.backend.auth.support.IdentifierNormalizer;
import com.stu.edu.vn.backend.auth.support.NormalizedIdentifier;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.TokenHashService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cài đặt AuthService cho luồng đăng ký MVP: tạo tài khoản, hồ sơ rỗng và phiên đăng nhập.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int REFRESH_TOKEN_RANDOM_BYTES = 48;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHashService tokenHashService;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            TokenHashService tokenHashService,
            CurrentUserProvider currentUserProvider,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenHashService = tokenHashService;
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validatePasswordConfirmation(request);
        NormalizedIdentifier identifier = IdentifierNormalizer.normalize(request.identifier());
        ensureIdentifierAvailable(identifier);

        User user = createUser(identifier, passwordEncoder.encode(request.password()));
        User savedUser = saveUserOrThrowDuplicate(user, identifier);
        saveEmptyProfile(savedUser);

        String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getId());
        saveRefreshToken(savedUser, refreshToken);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getRole(),
                savedUser.getStatus(),
                false,
                RegisterResponse.ONBOARDING_PROFILE,
                accessToken,
                refreshToken,
                accessTokenExpiresInSeconds()
        );
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        NormalizedIdentifier identifier = IdentifierNormalizer.normalize(request.identifier());
        User user = findUserForLogin(identifier)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // So sánh mật khẩu bằng PasswordEncoder để không xử lý password thô thủ công.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Chỉ sau khi mật khẩu đúng mới trả USER_BLOCKED, tránh tiết lộ tài khoản có tồn tại hay không.
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = generateSecureRefreshToken();
        saveLoginRefreshToken(user, refreshToken, request.deviceId(), request.deviceInfo(), ipAddress);

        return new LoginResponse(
                accessToken,
                refreshToken,
                LoginResponse.BEARER_TOKEN_TYPE,
                accessTokenExpiresInSeconds(),
                refreshTokenExpiresInSeconds(),
                profile.getProfileCompletedAt() != null,
                new LoginResponse.UserSummary(user.getId(), user.getRole())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        // Backend chỉ xử lý hash của Refresh Token, không dùng Access Token để tìm phiên refresh.
        String tokenHash = tokenHashService.sha256Hex(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // Token đã thu hồi luôn bị từ chối, kể cả khi thời hạn vẫn còn.
        if (refreshToken.getRevokedAt() != null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        // Dùng Clock để kiểm tra thời hạn ổn định trong test và không hard-code thời gian.
        if (!refreshToken.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(refreshToken.getUser().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());

        return new RefreshTokenResponse(
                accessToken,
                RefreshTokenResponse.BEARER_TOKEN_TYPE,
                accessTokenExpiresInSeconds(),
                profile.getProfileCompletedAt() != null
        );
    }

    @Override
    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        // Logout chỉ dùng Refresh Token từ body để tìm phiên cần thu hồi, không dùng Access Token làm khóa phiên.
        String tokenHash = tokenHashService.sha256Hex(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // Không cho người dùng thu hồi Refresh Token thuộc tài khoản khác.
        if (!refreshToken.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // Logout lặp lại là idempotent: token đã revoke vẫn trả thành công và không cập nhật lại.
        if (refreshToken.getRevokedAt() != null) {
            return new LogoutResponse(true);
        }

        // Chỉ token còn trong thời hạn mới cần đánh dấu revoked_at; token hết hạn đã không thể refresh.
        if (refreshToken.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            refreshToken.setRevokedAt(LocalDateTime.now(clock));
            refreshTokenRepository.saveAndFlush(refreshToken);
        }

        return new LogoutResponse(true);
    }

    private void validatePasswordConfirmation(RegisterRequest request) {
        if (request.password() == null || !request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }
    }

    private void ensureIdentifierAvailable(NormalizedIdentifier identifier) {
        if (identifier.type() == IdentifierType.EMAIL && userRepository.existsByEmail(identifier.value())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (identifier.type() == IdentifierType.PHONE_NUMBER && userRepository.existsByPhoneNumber(identifier.value())) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }
    }

    private User createUser(NormalizedIdentifier identifier, String passwordHash) {
        if (identifier.type() == IdentifierType.EMAIL) {
            return new User(identifier.value(), null, passwordHash);
        }
        return new User(null, identifier.value(), passwordHash);
    }

    private User saveUserOrThrowDuplicate(User user, NormalizedIdentifier identifier) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw duplicateException(identifier);
        }
    }

    private void saveEmptyProfile(User user) {
        try {
            userProfileRepository.saveAndFlush(new UserProfile(user));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED);
        }
    }

    private void saveRefreshToken(User user, String rawRefreshToken) {
        try {
            String tokenHash = tokenHashService.sha256Hex(rawRefreshToken);
            LocalDateTime expiresAt = LocalDateTime.now(clock)
                    .plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()));
            refreshTokenRepository.saveAndFlush(new RefreshToken(user, tokenHash, expiresAt));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED);
        }
    }

    private BusinessException duplicateException(NormalizedIdentifier identifier) {
        if (identifier.type() == IdentifierType.EMAIL) {
            return new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return new BusinessException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }

    private long accessTokenExpiresInSeconds() {
        return Duration.ofMillis(jwtProperties.getAccessTokenExpirationMillis()).toSeconds();
    }

    private long refreshTokenExpiresInSeconds() {
        return Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()).toSeconds();
    }

    private Optional<User> findUserForLogin(NormalizedIdentifier identifier) {
        if (identifier.type() == IdentifierType.EMAIL) {
            return userRepository.findByEmail(identifier.value());
        }
        return userRepository.findByPhoneNumber(identifier.value());
    }

    private String generateSecureRefreshToken() {
        // Refresh Token là chuỗi ngẫu nhiên đủ dài, không chứa thông tin người dùng và chỉ lưu hash trong database.
        byte[] randomBytes = new byte[REFRESH_TOKEN_RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private void saveLoginRefreshToken(
            User user,
            String rawRefreshToken,
            String deviceId,
            String deviceInfo,
            String ipAddress
    ) {
        try {
            String tokenHash = tokenHashService.sha256Hex(rawRefreshToken);
            LocalDateTime expiresAt = LocalDateTime.now(clock)
                    .plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()));
            RefreshToken refreshToken = new RefreshToken(
                    user,
                    tokenHash,
                    normalizeOptionalMetadata(deviceId),
                    normalizeOptionalMetadata(deviceInfo),
                    normalizeOptionalMetadata(ipAddress),
                    expiresAt
            );
            refreshTokenRepository.saveAndFlush(refreshToken);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String normalizeOptionalMetadata(String value) {
        if (value == null) {
            return null;
        }
        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }
}
