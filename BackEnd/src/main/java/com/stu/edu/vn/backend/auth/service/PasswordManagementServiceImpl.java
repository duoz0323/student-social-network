package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.ChangePasswordRequest;
import com.stu.edu.vn.backend.auth.dto.PasswordMutationResponse;
import com.stu.edu.vn.backend.auth.dto.SetPasswordRequest;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Thiết lập/đổi mật khẩu và thu hồi phiên trong cùng transaction. */
@Service
public class PasswordManagementServiceImpl implements PasswordManagementService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final ReauthenticationChallengeRepository challengeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final PasswordEncoder passwordEncoder;
    private final AuthHmacService hmacService;
    private final Clock clock;

    public PasswordManagementServiceImpl(CurrentUserProvider currentUserProvider, UserRepository userRepository,
            ReauthenticationChallengeRepository challengeRepository, RefreshTokenRepository refreshTokenRepository,
            PasswordPolicyValidator passwordPolicyValidator, PasswordEncoder passwordEncoder,
            AuthHmacService hmacService, Clock clock) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.passwordEncoder = passwordEncoder;
        this.hmacService = hmacService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PasswordMutationResponse setPassword(String rawToken, SetPasswordRequest request) {
        validateNewPassword(request.newPassword(), request.confirmPassword());
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_REQUIRED);
        }
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now(clock);
        User user = activeUser(userId);
        if (user.getEmail() == null || user.getEmailVerifiedAt() == null) {
            throw new BusinessException(ErrorCode.AUTH_IDENTIFIER_NOT_VERIFIED);
        }
        if (user.getPasswordHash() != null) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_ALREADY_CONFIGURED);
        }
        ReauthenticationChallenge challenge = challengeRepository
                .findByTokenHashForUpdate(hmacService.hashFlowToken(rawToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_INVALID));
        validateSetPasswordChallenge(challenge, userId, now);

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        challenge.consume(now);
        userRepository.saveAndFlush(user);
        challengeRepository.saveAndFlush(challenge);
        refreshTokenRepository.revokeAllActiveByUserId(userId, now);
        return new PasswordMutationResponse(true, true);
    }

    @Override
    @Transactional
    public PasswordMutationResponse changePassword(ChangePasswordRequest request) {
        validateNewPassword(request.newPassword(), request.confirmPassword());
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now(clock);
        User user = activeUser(userId);
        if (user.getEmail() == null || user.getEmailVerifiedAt() == null || user.getPasswordHash() == null) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_LOGIN_NOT_AVAILABLE);
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_CURRENT_PASSWORD_INCORRECT);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_MUST_BE_DIFFERENT);
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.saveAndFlush(user);
        refreshTokenRepository.revokeAllActiveByUserId(userId, now);
        return new PasswordMutationResponse(true, true);
    }

    private User activeUser(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        return user;
    }

    private void validateSetPasswordChallenge(ReauthenticationChallenge challenge, Long userId, LocalDateTime now) {
        if (!challenge.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_INVALID);
        }
        if (challenge.getScope() != ReauthenticationScope.SET_PASSWORD
                || challenge.getTargetAuthMethod() != AuthMethod.EMAIL) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_PURPOSE_INVALID);
        }
        if (challenge.getStatus() != ReauthenticationChallengeStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_ALREADY_USED);
        }
        if (!challenge.getExpiresAt().isAfter(now)) {
            challenge.expire(now);
            challengeRepository.saveAndFlush(challenge);
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_EXPIRED);
        }
    }

    private void validateNewPassword(String password, String confirmation) {
        if (!Objects.equals(password, confirmation)) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_CONFIRMATION_MISMATCH);
        }
        if (!passwordPolicyValidator.isValid(password)) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        }
    }
}
