package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transaction duy nhất cho lock, OTP, account, profile, Refresh Token và completion của pending. */
@Service
@RequiredArgsConstructor
public class RegistrationVerificationTransactionService {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthHmacService authHmacService;
    private final AuthRegistrationProperties registrationProperties;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Transactional
    public RegistrationVerificationResult verify(VerifyRegistrationRequest request, String ipAddress) {
        String flowTokenHash = authHmacService.hashFlowToken(request.registrationFlowToken());
        PendingRegistration pending = pendingRegistrationRepository.findByFlowTokenHashForUpdate(flowTokenHash)
                .orElse(null);
        if (pending == null) {
            return RegistrationVerificationResult.failure(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);
        }

        if (pending.getStatus() == OtpChallengeStatus.COMPLETED) {
            return RegistrationVerificationResult.failure(ErrorCode.AUTH_REGISTRATION_ALREADY_COMPLETED);
        }
        if (pending.getStatus() == OtpChallengeStatus.CANCELLED) {
            return RegistrationVerificationResult.failure(ErrorCode.AUTH_REGISTRATION_CANCELLED);
        }
        if (pending.getStatus() == OtpChallengeStatus.EXPIRED) {
            return RegistrationVerificationResult.failure(ErrorCode.AUTH_REGISTRATION_EXPIRED);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (!pending.getExpiresAt().isAfter(now)) {
            // Expire và xóa secret ngay trong transaction trước khi trả lỗi cho Client.
            pending.expire(now);
            pendingRegistrationRepository.saveAndFlush(pending);
            return RegistrationVerificationResult.failure(ErrorCode.AUTH_REGISTRATION_EXPIRED);
        }
        if (!pending.getOtpExpiresAt().isAfter(now)) {
            return RegistrationVerificationResult.failure(ErrorCode.AUTH_OTP_EXPIRED);
        }
        if (pending.getFailedAttempts() >= registrationProperties.getMaxOtpAttempts()) {
            return RegistrationVerificationResult.failure(ErrorCode.AUTH_OTP_ATTEMPTS_EXCEEDED);
        }

        if (!authHmacService.verifyOtp(request.code(), pending.getOtpHash())) {
            // Row lock bảo đảm hai request OTP sai đồng thời không làm mất lần tăng failed_attempts.
            pending.recordFailedAttempt();
            pendingRegistrationRepository.saveAndFlush(pending);
            ErrorCode error = pending.getFailedAttempts() >= registrationProperties.getMaxOtpAttempts()
                    ? ErrorCode.AUTH_OTP_ATTEMPTS_EXCEEDED
                    : ErrorCode.AUTH_OTP_INVALID;
            return RegistrationVerificationResult.failure(error);
        }

        ensureIdentifierStillAvailable(pending);
        User user = createVerifiedUser(pending, now);
        User savedUser = saveUserWithRaceMapping(user, pending.getRegistrationType());
        userProfileRepository.saveAndFlush(new UserProfile(savedUser));

        IssuedRefreshToken refreshToken = refreshTokenIssuer.issue(
                savedUser,
                request.deviceId(),
                request.deviceInfo(),
                ipAddress
        );
        String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getRole().name());

        // Lifecycle giải phóng active key, xóa OTP/password và giữ HMAC lookup hash trong retention.
        pending.complete(savedUser, now);
        pendingRegistrationRepository.saveAndFlush(pending);

        VerifyRegistrationResponse response = new VerifyRegistrationResponse(
                accessToken,
                refreshToken.rawToken(),
                VerifyRegistrationResponse.BEARER_TOKEN_TYPE,
                Duration.ofMillis(jwtProperties.getAccessTokenExpirationMillis()).toSeconds(),
                refreshToken.expiresInSeconds(),
                false,
                VerifyRegistrationResponse.NEXT_STEP_ONBOARDING,
                new VerifyRegistrationResponse.UserSummary(savedUser.getId(), savedUser.getRole())
        );
        return RegistrationVerificationResult.success(response);
    }

    private void ensureIdentifierStillAvailable(PendingRegistration pending) {
        if (pending.getRegistrationType() == RegistrationType.EMAIL
                && userRepository.existsByEmail(pending.getIdentifierNormalized())) {
            throw new RegistrationIdentifierConflictException(RegistrationType.EMAIL, null);
        }
    }

    private User createVerifiedUser(PendingRegistration pending, LocalDateTime verifiedAt) {
        User user = new User(pending.getIdentifierNormalized(), pending.getPasswordHash());
        user.setEmailVerifiedAt(verifiedAt);
        return user;
    }

    private User saveUserWithRaceMapping(User user, RegistrationType registrationType) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new RegistrationIdentifierConflictException(registrationType, exception);
        }
    }
}
