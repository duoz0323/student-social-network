package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.AuthMethodResponse;
import com.stu.edu.vn.backend.auth.entity.AuthMethodLinkChallenge;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.AuthMethodLinkChallengeRepository;
import com.stu.edu.vn.backend.auth.support.IdentifierMasker;
import com.stu.edu.vn.backend.auth.support.IdentifierType;
import com.stu.edu.vn.backend.auth.support.NormalizedIdentifier;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transaction ngắn cho lifecycle OTP liên kết email/phone. */
@Service
public class AuthMethodLinkTransactionService {
    private static final Duration LINK_EXPIRATION = Duration.ofMinutes(15);

    private final AuthMethodLinkChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final OtpGenerator otpGenerator;
    private final FlowTokenGenerator flowTokenGenerator;
    private final AuthHmacService hmacService;
    private final AuthRegistrationProperties properties;
    private final IdentifierMasker masker;
    private final Clock clock;

    public AuthMethodLinkTransactionService(AuthMethodLinkChallengeRepository challengeRepository,
            UserRepository userRepository, OtpGenerator otpGenerator, FlowTokenGenerator flowTokenGenerator,
            AuthHmacService hmacService, AuthRegistrationProperties properties, IdentifierMasker masker, Clock clock) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.otpGenerator = otpGenerator;
        this.flowTokenGenerator = flowTokenGenerator;
        this.hmacService = hmacService;
        this.properties = properties;
        this.masker = masker;
        this.clock = clock;
    }

    @Transactional
    public LinkChallengeCreation start(Long userId, NormalizedIdentifier identifier, AuthMethodLinkPurpose purpose) {
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
                identifier.type() == IdentifierType.EMAIL ? RegistrationType.EMAIL : RegistrationType.PHONE,
                purpose, identifier.value(), otpExpires, challenge.getResendAvailableAt(), expiresAt);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AuthMethodResponse verify(Long userId, String rawFlowToken, String rawOtp,
            AuthMethodLinkPurpose expectedPurpose) {
        if (rawFlowToken == null || rawFlowToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID);
        }
        AuthMethodLinkChallenge challenge = challengeRepository
                .findByFlowTokenHashForUpdate(hmacService.hashFlowToken(rawFlowToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_METHOD_LINK_CHALLENGE_INVALID));
        LocalDateTime now = LocalDateTime.now(clock);
        validateChallenge(challenge, userId, expectedPurpose, now);
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

        User user = activeUser(userId);
        String identifier = challenge.getIdentifierNormalized();
        if (expectedPurpose == AuthMethodLinkPurpose.LINK_EMAIL) {
            if (user.getEmail() != null && user.getEmailVerifiedAt() != null)
                throw new BusinessException(ErrorCode.AUTH_METHOD_ALREADY_LINKED);
            if (userRepository.findByEmail(identifier).filter(owner -> !owner.getId().equals(userId)).isPresent()) {
                throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_IN_USE);
            }
            user.setEmail(identifier);
            user.setEmailVerifiedAt(now);
        } else {
            if (user.getPhoneNumber() != null && user.getPhoneVerifiedAt() != null)
                throw new BusinessException(ErrorCode.AUTH_METHOD_ALREADY_LINKED);
            if (userRepository.findByPhoneNumber(identifier).filter(owner -> !owner.getId().equals(userId)).isPresent()) {
                throw new BusinessException(ErrorCode.AUTH_PHONE_ALREADY_IN_USE);
            }
            user.setPhoneNumber(identifier);
            user.setPhoneVerifiedAt(now);
        }
        userRepository.saveAndFlush(user);
        AuthMethod method = expectedPurpose == AuthMethodLinkPurpose.LINK_EMAIL ? AuthMethod.EMAIL : AuthMethod.PHONE;
        String masked = masker.mask(new NormalizedIdentifier(
                method == AuthMethod.EMAIL ? IdentifierType.EMAIL : IdentifierType.PHONE_NUMBER, identifier));
        challenge.complete(now);
        challengeRepository.saveAndFlush(challenge);
        return new AuthMethodResponse(method, masked, true, now, false, user.getPasswordHash() != null);
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
        RegistrationType type = purpose == AuthMethodLinkPurpose.LINK_EMAIL
                ? RegistrationType.EMAIL : RegistrationType.PHONE;
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

    private void ensureAvailable(User user, NormalizedIdentifier identifier) {
        if (identifier.type() == IdentifierType.EMAIL) {
            if (user.getEmail() != null && user.getEmailVerifiedAt() != null)
                throw new BusinessException(ErrorCode.AUTH_METHOD_ALREADY_LINKED);
            if (userRepository.existsByEmail(identifier.value()))
                throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_IN_USE);
        } else {
            if (user.getPhoneNumber() != null && user.getPhoneVerifiedAt() != null)
                throw new BusinessException(ErrorCode.AUTH_METHOD_ALREADY_LINKED);
            if (userRepository.existsByPhoneNumber(identifier.value()))
                throw new BusinessException(ErrorCode.AUTH_PHONE_ALREADY_IN_USE);
        }
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

    private void validateType(NormalizedIdentifier identifier, AuthMethodLinkPurpose purpose) {
        boolean valid = (purpose == AuthMethodLinkPurpose.LINK_EMAIL && identifier.type() == IdentifierType.EMAIL)
                || (purpose == AuthMethodLinkPurpose.LINK_PHONE && identifier.type() == IdentifierType.PHONE_NUMBER);
        if (!valid) throw new BusinessException(ErrorCode.AUTH_IDENTIFIER_INVALID);
    }

    private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private String normalizeFailure(String code) {
        return code != null && code.matches("[A-Z0-9_]{1,64}") ? code : "DELIVERY_FAILED";
    }
}
