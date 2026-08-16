package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.mapper.AuthMapper;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.TokenHashService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cài đặt các use case Auth cũ đang tồn tại; đăng ký local mới không còn tạo user tại đây.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHashService tokenHashService;
    private final AuthMapper authMapper;
    private final Clock clock;
    private final RefreshTokenIssuer refreshTokenIssuer;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        NormalizedEmail email = EmailNormalizer.normalize(request.email());
        User user = userRepository.findByEmail(email.value())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getAccountType() == UserAccountType.MANAGED) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Trạng thái tài khoản phải được kiểm tra trước khi phát hành bất kỳ phiên đăng nhập nào.
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        ensureEmailVerified(user);

        // Social-only account không có password hash nên không được đi vào PasswordEncoder.matches.
        if (user.getPasswordHash() == null) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_LOGIN_NOT_AVAILABLE);
        }

        // So sánh mật khẩu bằng PasswordEncoder để không xử lý password thô thủ công.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_LOGIN_FAILED));
        boolean profileCompleted = profile.isCompleted();
        IssuedRefreshToken refreshToken = issueRefreshToken(user, request, ipAddress);
        String accessToken = generateAccessToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken.rawToken(),
                LoginResponse.BEARER_TOKEN_TYPE,
                accessTokenExpiresInSeconds(),
                refreshToken.expiresInSeconds(),
                profileCompleted,
                profileCompleted
                        ? LoginResponse.NEXT_STEP_HOME
                        : LoginResponse.NEXT_STEP_COMPLETE_PROFILE,
                authMapper.toUserSummary(user)
        );
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        // Backend chỉ xử lý hash của Refresh Token, không dùng Access Token để tìm phiên refresh.
        String tokenHash = tokenHashService.sha256Hex(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
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
        if (user.getAccountType() == UserAccountType.MANAGED) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);
        refreshToken.revoke(now);

        String accessToken;
        IssuedRefreshToken newRefreshToken;
        try {
            accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());
            newRefreshToken = refreshTokenIssuer.issue(
                    user,
                    refreshToken.getDeviceId(),
                    refreshToken.getDeviceInfo(),
                    refreshToken.getIpAddress()
            );
        } catch (RuntimeException exception) {
            // BusinessException buộc transaction rollback để token cũ không bị revoke khi rotation dở dang.
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_ROTATION_FAILED);
        }

        return new RefreshTokenResponse(
                accessToken,
                newRefreshToken.rawToken(),
                RefreshTokenResponse.BEARER_TOKEN_TYPE,
                accessTokenExpiresInSeconds(),
                newRefreshToken.expiresInSeconds(),
                profile.isCompleted()
        );
    }

    @Override
    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        // Logout chỉ dùng Refresh Token từ body để tìm phiên cần thu hồi, không dùng Access Token làm khóa phiên.
        String tokenHash = tokenHashService.sha256Hex(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // Logout lặp lại là idempotent: token đã revoke vẫn trả thành công và không cập nhật lại.
        if (refreshToken.getRevokedAt() != null) {
            return new LogoutResponse(true);
        }

        try {
            // Token hết hạn cũng được đánh dấu thu hồi để trạng thái phiên kết thúc rõ ràng và logout vẫn idempotent.
            refreshToken.revoke(LocalDateTime.now(clock));
            refreshTokenRepository.saveAndFlush(refreshToken);
        } catch (DataAccessException exception) {
            throw new BusinessException(ErrorCode.AUTH_LOGOUT_FAILED);
        }

        return new LogoutResponse(true);
    }

    private long accessTokenExpiresInSeconds() {
        return Duration.ofMillis(jwtProperties.getAccessTokenExpirationMillis()).toSeconds();
    }

    private void ensureEmailVerified(User user) {
        boolean verified = user.getEmail() != null && user.getEmailVerifiedAt() != null;
        if (!verified) {
            throw new BusinessException(ErrorCode.AUTH_IDENTIFIER_NOT_VERIFIED);
        }
    }

    private IssuedRefreshToken issueRefreshToken(User user, LoginRequest request, String ipAddress) {
        try {
            // Refresh Token được tạo trong transaction login và chỉ hash mới được lưu xuống database.
            return refreshTokenIssuer.issue(user, request.deviceId(), request.deviceInfo(), ipAddress);
        } catch (DataAccessException exception) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_CREATION_FAILED);
        }
    }

    private String generateAccessToken(User user) {
        try {
            return jwtService.generateAccessToken(user.getId(), user.getRole().name());
        } catch (RuntimeException exception) {
            // Runtime exception làm rollback Refresh Token vừa tạo, tránh trả phiên đăng nhập dở dang.
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }
    }

}
