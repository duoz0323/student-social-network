package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.AuthMethodResponse;
import com.stu.edu.vn.backend.auth.dto.CompleteEmailLinkRequest;
import com.stu.edu.vn.backend.auth.dto.VerifiedEmailLinkResponse;
import com.stu.edu.vn.backend.auth.entity.AuthMethodLinkChallenge;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.AuthMethodLinkChallengeRepository;
import com.stu.edu.vn.backend.auth.support.EmailMasker;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Transaction ngắn cho lifecycle OTP liên kết email. */
@Service
public class AuthMethodLinkTransactionService {
    private static final Duration LINK_EXPIRATION = Duration.ofMinutes(15);

    private final AuthMethodLinkChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final OtpGenerator otpGenerator;
    private final FlowTokenGenerator flowTokenGenerator;
    private final AuthHmacService hmacService;
    private final AuthRegistrationProperties properties;
    private final EmailMasker masker;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AuthMethodLinkTransactionService(AuthMethodLinkChallengeRepository challengeRepository,
            UserRepository userRepository, OtpGenerator otpGenerator, FlowTokenGenerator flowTokenGenerator,
            AuthHmacService hmacService, AuthRegistrationProperties properties, EmailMasker masker,
            PasswordPolicyValidator passwordPolicyValidator, PasswordEncoder passwordEncoder, Clock clock) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.otpGenerator = otpGenerator;
        this.flowTokenGenerator = flowTokenGenerator;
        this.hmacService = hmacService;
        this.properties = properties;
        this.masker = masker;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public LinkChallengeCreation start(Long userId, NormalizedEmail identifier, AuthMethodLinkPurpose purpose) {
        User user = activeUser(userId);
        validateType(identifier, purpose);
        ensureAvailable(user, identifier);
        LocalDateTime now = LocalDateTime.now(clock);
        String userPurposeKey = userId + ":" + purpose.name();
        challengeRepository.findByActiveUserPurposeKeyForUpdate(userPurposeKey).ifPresent(existing -> {
            if (existing.getExpiresAt().isAfter(now)) {
                throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_ALREADY_PENDING);
            }
            existing.expire(now);
            challengeRepository.saveAndFlush(existing);
        });

        String rawFlow = flowTokenGenerator.generate();
        String rawOtp = otpGenerator.generate();
        LocalDateTime expiresAt = now.plus(LINK_EXPIRATION);
        LocalDateTime otpExpires = min(now.plus(properties.getOtpExpiration()), expiresAt);
        AuthMethodLinkChallenge challenge = AuthMethodLinkChallenge.start(user, purpose, identifier.value(),
                hmacService.hashFlowToken(rawFlow), hmacService.hashOtp(rawOtp), otpExpires,
                now.plus(properties.getResendCooldown()), expiresAt);
        challengeRepository.saveAndFlush(challenge);
        return new LinkChallengeCreation(challenge.getId(), challenge.getOtpVersion(), rawFlow,
                challenge.getFlowTokenHash(), rawOtp,
                RegistrationType.EMAIL,
                purpose, identifier.value(), otpExpires, challenge.getResendAvailableAt(), expiresAt);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public VerifiedEmailLinkResponse verifyOtp(Long userId, String rawFlowToken, String rawOtp) {
        if (rawFlowToken == null || rawFlowToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID);
        }
        AuthMethodLinkChallenge challenge = challengeRepository
                .findByFlowTokenHashForUpdate(hmacService.hashFlowToken(rawFlowToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID));
        LocalDateTime now = LocalDateTime.now(clock);
        validateChallenge(challenge, userId, AuthMethodLinkPurpose.LINK_EMAIL, now);
        if (challenge.getOtpVerifiedAt() != null) {
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_ALREADY_USED);
        }
        if (!challenge.getOtpExpiresAt().isAfter(now)) {
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_OTP_EXPIRED);
        }
        if (challenge.getFailedAttempts() >= properties.getMaxOtpAttempts()) {
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_OTP_ATTEMPTS_EXCEEDED);
        }
        if (!hmacService.verifyOtp(rawOtp, challenge.getOtpHash())) {
            challenge.recordFailedAttempt();
            challengeRepository.saveAndFlush(challenge);
            throw new BusinessException(challenge.getFailedAttempts() >= properties.getMaxOtpAttempts()
                    ? ErrorCode.AUTH_METHOD_LINK_OTP_ATTEMPTS_EXCEEDED
                    : ErrorCode.AUTH_METHOD_LINK_OTP_INVALID);
        }

        String rotatedFlow = flowTokenGenerator.generate();
        challenge.verifyOtp(hmacService.hashFlowToken(rotatedFlow), now);
        challengeRepository.saveAndFlush(challenge);
        return new VerifiedEmailLinkResponse(rotatedFlow,
                masker.mask(new NormalizedEmail(challenge.getIdentifierNormalized())), challenge.getExpiresAt());
    }

    @Transactional
    public AuthMethodResponse complete(Long userId, String rawFlowToken, CompleteEmailLinkRequest request) {
        validatePassword(request.newPassword(), request.confirmPassword());
        if (rawFlowToken == null || rawFlowToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID);
        }
        AuthMethodLinkChallenge challenge = challengeRepository
                .findByFlowTokenHashForUpdate(hmacService.hashFlowToken(rawFlowToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID));
        LocalDateTime now = LocalDateTime.now(clock);
        validateChallenge(challenge, userId, AuthMethodLinkPurpose.LINK_EMAIL, now);
        if (challenge.getOtpVerifiedAt() == null) {
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_OTP_INVALID);
        }

        User user = activeUser(userId);
        String identifier = challenge.getIdentifierNormalized();
        if (user.getEmail() != null && user.getEmailVerifiedAt() != null)
            throw new BusinessException(ErrorCode.AUTH_METHOD_ALREADY_LINKED);
        if (userRepository.findByEmail(identifier).filter(owner -> !owner.getId().equals(userId)).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_IN_USE);
        }
        user.setEmail(identifier);
        user.setEmailVerifiedAt(now);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.saveAndFlush(user);
        AuthMethod method = AuthMethod.EMAIL;
        String masked = masker.mask(new NormalizedEmail(identifier));
        challenge.complete(now);
        challengeRepository.saveAndFlush(challenge);
        return new AuthMethodResponse(method, masked, true, true, now,
                false, false, true, true,
                com.stu.edu.vn.backend.auth.enums.EmailLoginState.READY, false, true);
    }

    @Transactional
    public LinkChallengeCreation resend(Long userId, String rawFlowToken, AuthMethodLinkPurpose purpose) {
        if (rawFlowToken == null || rawFlowToken.isBlank())
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID);
        AuthMethodLinkChallenge challenge = challengeRepository
                .findByFlowTokenHashForUpdate(hmacService.hashFlowToken(rawFlowToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID));
        LocalDateTime now = LocalDateTime.now(clock);
        validateChallenge(challenge, userId, purpose, now);
        if (challenge.getOtpVerifiedAt() != null)
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_ALREADY_USED);
        activeUser(userId);
        if (challenge.getResendAvailableAt().isAfter(now))
            throw new BusinessException(ErrorCode.AUTH_OTP_RESEND_TOO_SOON);
        String identifier = challenge.getIdentifierNormalized();
        String rawFlow = flowTokenGenerator.generate();
        String rawOtp = otpGenerator.generate();
        LocalDateTime otpExpires = min(now.plus(properties.getOtpExpiration()), challenge.getExpiresAt());
        challenge.resend(hmacService.hashFlowToken(rawFlow), hmacService.hashOtp(rawOtp), otpExpires,
                now.plus(properties.getResendCooldown()));
        challengeRepository.saveAndFlush(challenge);
        RegistrationType type = RegistrationType.EMAIL;
        return new LinkChallengeCreation(challenge.getId(), challenge.getOtpVersion(), rawFlow,
                challenge.getFlowTokenHash(), rawOtp, type, purpose, identifier,
                otpExpires, challenge.getResendAvailableAt(), challenge.getExpiresAt());
    }

    @Transactional
    public void recordDelivery(Long id, int version, com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult result) {
        AuthMethodLinkChallenge challenge = challengeRepository.findByIdForUpdate(id).orElse(null);
        if (challenge == null || challenge.getStatus() != OtpChallengeStatus.PENDING
                || challenge.getOtpVersion() != version) return;
        LocalDateTime now = LocalDateTime.now(clock);
        switch (result.outcome()) {
            case SENT -> challenge.markDeliverySent(now);
            case FAILED -> challenge.markDeliveryFailed(now, normalizeFailure(result.failureCode()));
            case UNKNOWN -> challenge.markDeliveryUnknown(now);
        }
        challengeRepository.saveAndFlush(challenge);
    }

    private User activeUser(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.USER_BLOCKED);
        return user;
    }

    private void ensureAvailable(User user, NormalizedEmail identifier) {
        if (user.getEmail() != null && user.getEmailVerifiedAt() != null)
            throw new BusinessException(ErrorCode.AUTH_METHOD_ALREADY_LINKED);
        if (userRepository.existsByEmail(identifier.value()))
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_IN_USE);
    }

    private void validateChallenge(AuthMethodLinkChallenge challenge, Long userId,
            AuthMethodLinkPurpose purpose, LocalDateTime now) {
        if (!challenge.getUser().getId().equals(userId) || challenge.getPurpose() != purpose)
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID);
        if (challenge.getStatus() != OtpChallengeStatus.PENDING)
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_ALREADY_USED);
        if (!challenge.getExpiresAt().isAfter(now)) {
            challenge.expire(now);
            challengeRepository.saveAndFlush(challenge);
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_EXPIRED);
        }
    }

    private void validateType(NormalizedEmail identifier, AuthMethodLinkPurpose purpose) {
        if (purpose != AuthMethodLinkPurpose.LINK_EMAIL) {
            throw new BusinessException(ErrorCode.AUTH_IDENTIFIER_INVALID);
        }
    }

    private void validatePassword(String password, String confirmation) {
        if (!Objects.equals(password, confirmation)) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_CONFIRMATION_MISMATCH);
        }
        if (!passwordPolicyValidator.isValid(password)) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        }
    }

    private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private String normalizeFailure(String code) {
        return code != null && code.matches("[A-Z0-9_]{1,64}") ? code : "DELIVERY_FAILED";
    }
}
