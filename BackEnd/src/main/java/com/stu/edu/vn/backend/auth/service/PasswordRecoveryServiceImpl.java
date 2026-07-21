package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.PasswordRecoveryProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.delivery.PasswordRecoveryOtpRequested;
import com.stu.edu.vn.backend.auth.dto.*;
import com.stu.edu.vn.backend.auth.entity.PasswordRecoveryChallenge;
import com.stu.edu.vn.backend.auth.enums.PasswordRecoveryStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.PasswordRecoveryChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orchestrates recovery với response trung tính và transaction boundary rõ ràng. */
@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {
    private static final String FLOW_TYPE = "PASSWORD_RECOVERY";
    private final PasswordRecoveryChallengeRepository challenges;
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final AuthHmacService hmac;
    private final FlowTokenGenerator flowTokens;
    private final OtpGenerator otps;
    private final PasswordRecoveryProperties properties;
    private final PasswordPolicyValidator passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public PasswordRecoveryServiceImpl(PasswordRecoveryChallengeRepository challenges, UserRepository users,
            RefreshTokenRepository refreshTokens, AuthHmacService hmac, FlowTokenGenerator flowTokens,
            OtpGenerator otps, PasswordRecoveryProperties properties, PasswordPolicyValidator passwordPolicy,
            PasswordEncoder passwordEncoder, ApplicationEventPublisher events, Clock clock) {
        this.challenges = challenges; this.users = users; this.refreshTokens = refreshTokens; this.hmac = hmac;
        this.flowTokens = flowTokens; this.otps = otps; this.properties = properties;
        this.passwordPolicy = passwordPolicy; this.passwordEncoder = passwordEncoder; this.events = events; this.clock = clock;
    }

    @Override
    @Transactional
    public PasswordRecoveryChallengeResponse start(StartPasswordRecoveryRequest request) {
        NormalizedEmail identifier;
        try { identifier = EmailNormalizer.normalize(request.email()); }
        catch (RuntimeException exception) { identifier = null; }
        String opaqueSubject = hmac.hashFlowToken("password-recovery:" +
                (identifier == null ? request.email().trim().toLowerCase() : identifier.value()));
        LocalDateTime now = LocalDateTime.now(clock);
        var existing = challenges.findActiveBySubjectForUpdate(opaqueSubject).orElse(null);
        if (existing != null && existing.getChallengeExpiresAt().isAfter(now)) {
            return restartExisting(existing, identifier, now);
        }
        if (existing != null) existing.expire(now);
        User eligible = findEligible(identifier);
        RegistrationType channel = RegistrationType.EMAIL;
        String rawFlow = flowTokens.generate();
        String rawOtp = otps.generate();
        LocalDateTime challengeExpiry = now.plus(properties.getChallengeExpiration());
        LocalDateTime otpExpiry = minimum(now.plus(properties.getOtpExpiration()), challengeExpiry);
        PasswordRecoveryChallenge challenge = PasswordRecoveryChallenge.start(eligible, opaqueSubject, channel,
                hmac.hashFlowToken(rawFlow), hmac.hashOtp(rawOtp), otpExpiry,
                now.plus(properties.getResendCooldown()), challengeExpiry);
        try { challenges.saveAndFlush(challenge); }
        catch (DataIntegrityViolationException exception) { throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_START_FAILED); }
        publishIfReal(challenge, identifier, rawOtp);
        return response(challenge, rawFlow);
    }

    private PasswordRecoveryChallengeResponse restartExisting(PasswordRecoveryChallenge challenge,
            NormalizedEmail identifier, LocalDateTime now) {
        String rawFlow = flowTokens.generate();
        if (!now.isBefore(challenge.getResendAvailableAt())) {
            String rawOtp = otps.generate();
            challenge.rotate(hmac.hashFlowToken(rawFlow), hmac.hashOtp(rawOtp),
                    minimum(now.plus(properties.getOtpExpiration()), challenge.getChallengeExpiresAt()),
                    now.plus(properties.getResendCooldown()));
            publishIfReal(challenge, identifier, rawOtp);
        } else challenge.rotateFlowToken(hmac.hashFlowToken(rawFlow));
        return response(challenge, rawFlow);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public VerifyPasswordRecoveryResponse verify(String rawFlow, VerifyPasswordRecoveryRequest request) {
        PasswordRecoveryChallenge challenge = flowChallenge(rawFlow);
        LocalDateTime now = LocalDateTime.now(clock);
        validatePending(challenge, now);
        if (!now.isBefore(challenge.getOtpExpiresAt())) throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_EXPIRED);
        boolean valid = challenge.getUser() != null && hmac.verifyOtp(request.code(), challenge.getOtpHash());
        if (!valid) {
            challenge.recordFailure(properties.getMaxOtpAttempts(), now);
            if (challenge.getStatus() == PasswordRecoveryStatus.LOCKED)
                throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_ATTEMPTS_EXCEEDED);
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_INVALID);
        }
        // Eligibility được kiểm tra lại để tài khoản vừa bị khóa không thể tiếp tục recovery.
        User user = users.findByIdForUpdate(challenge.getUser().getId()).orElseThrow(
                () -> new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_FLOW_INVALID));
        if (!isEligible(user, challenge.getDeliveryChannel()))
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_FLOW_INVALID);
        String rawReset = flowTokens.generate();
        LocalDateTime resetExpiry = now.plus(properties.getResetTokenExpiration());
        challenge.verify(hmac.hashFlowToken(rawReset), resetExpiry, now);
        return new VerifyPasswordRecoveryResponse(rawReset, resetExpiry);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public PasswordRecoveryChallengeResponse resend(String rawFlow) {
        PasswordRecoveryChallenge challenge = flowChallenge(rawFlow);
        LocalDateTime now = LocalDateTime.now(clock);
        validatePending(challenge, now);
        if (now.isBefore(challenge.getResendAvailableAt()))
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_RESEND_TOO_SOON);
        String newFlow = flowTokens.generate();
        String rawOtp = otps.generate();
        challenge.rotate(hmac.hashFlowToken(newFlow), hmac.hashOtp(rawOtp),
                minimum(now.plus(properties.getOtpExpiration()), challenge.getChallengeExpiresAt()),
                now.plus(properties.getResendCooldown()));
        publishIfReal(challenge, destination(challenge), rawOtp);
        return response(challenge, newFlow);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public CompletePasswordRecoveryResponse complete(String rawReset, CompletePasswordRecoveryRequest request) {
        if (rawReset == null || rawReset.isBlank()) throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID);
        PasswordRecoveryChallenge challenge = challenges.findByResetHashForUpdate(hmac.hashFlowToken(rawReset))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID));
        LocalDateTime now = LocalDateTime.now(clock);
        if (challenge.getStatus() == PasswordRecoveryStatus.COMPLETED)
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_USED);
        if (challenge.getStatus() != PasswordRecoveryStatus.VERIFIED)
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID);
        if (!now.isBefore(challenge.getResetTokenExpiresAt())) {
            challenge.expire(now); throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_EXPIRED);
        }
        if (!request.newPassword().equals(request.confirmPassword()))
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_CONFIRMATION_MISMATCH);
        if (!passwordPolicy.isValid(request.newPassword()))
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        User user = users.findByIdForUpdate(challenge.getUser().getId()).orElseThrow(
                () -> new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID));
        if (!isEligible(user, challenge.getDeliveryChannel()))
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID);
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash()))
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_MUST_BE_DIFFERENT);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        challenge.complete(now);
        refreshTokens.revokeAllActiveByUserId(user.getId(), now);
        return new CompletePasswordRecoveryResponse(true);
    }

    private PasswordRecoveryChallenge flowChallenge(String rawFlow) {
        if (rawFlow == null || rawFlow.isBlank()) throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_FLOW_INVALID);
        return challenges.findByFlowHashForUpdate(hmac.hashFlowToken(rawFlow))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_FLOW_INVALID));
    }
    private void validatePending(PasswordRecoveryChallenge challenge, LocalDateTime now) {
        if (challenge.getStatus() == PasswordRecoveryStatus.LOCKED)
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_OTP_ATTEMPTS_EXCEEDED);
        if (challenge.getStatus() != PasswordRecoveryStatus.PENDING)
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_FLOW_ALREADY_USED);
        if (!now.isBefore(challenge.getChallengeExpiresAt())) {
            challenge.expire(now); throw new BusinessException(ErrorCode.AUTH_PASSWORD_RECOVERY_FLOW_EXPIRED);
        }
    }
    private User findEligible(NormalizedEmail identifier) {
        if (identifier == null) return null;
        User user = users.findByEmail(identifier.value()).orElse(null);
        RegistrationType channel = RegistrationType.EMAIL;
        return isEligible(user, channel) ? user : null;
    }
    private boolean isEligible(User user, RegistrationType channel) {
        if (user == null || user.getStatus() != UserStatus.ACTIVE || user.getPasswordHash() == null) return false;
        return channel == RegistrationType.EMAIL ? user.getEmailVerifiedAt() != null : user.getEmailVerifiedAt() != null;
    }
    private void publishIfReal(PasswordRecoveryChallenge challenge, NormalizedEmail identifier, String otp) {
        if (challenge.getUser() != null && identifier != null)
            events.publishEvent(new PasswordRecoveryOtpRequested(challenge.getId(), challenge.getOtpVersion(),
                    challenge.getDeliveryChannel(), identifier.value(), otp));
    }
    private NormalizedEmail destination(PasswordRecoveryChallenge challenge) {
        User user = challenge.getUser();
        if (user == null) return null;
        return EmailNormalizer.normalize(challenge.getDeliveryChannel() == RegistrationType.EMAIL
                ? user.getEmail() : user.getEmail());
    }
    private PasswordRecoveryChallengeResponse response(PasswordRecoveryChallenge challenge, String rawFlow) {
        return new PasswordRecoveryChallengeResponse(true, FLOW_TYPE, rawFlow, challenge.getOtpExpiresAt(),
                challenge.getResendAvailableAt(), challenge.getChallengeExpiresAt());
    }
    private LocalDateTime minimum(LocalDateTime left, LocalDateTime right) { return left.isBefore(right) ? left : right; }
}
