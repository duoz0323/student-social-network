package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.RegistrationStatusResponse;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.support.IdentifierMasker;
import com.stu.edu.vn.backend.auth.support.IdentifierType;
import com.stu.edu.vn.backend.auth.support.NormalizedIdentifier;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction ngắn dùng cùng row lock với verify để mọi chuyển trạng thái có thứ tự nhất quán.
 */
@Service
@RequiredArgsConstructor
public class RegistrationLifecycleTransactionService {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final AuthHmacService authHmacService;
    private final OtpGenerator otpGenerator;
    private final AuthRegistrationProperties properties;
    private final IdentifierMasker identifierMasker;
    private final Clock clock;

    @Transactional
    public RegistrationResendResult issueNewOtp(String rawFlowToken) {
        PendingRegistration pending = findForUpdate(rawFlowToken);
        ErrorCode terminalError = terminalError(pending.getStatus());
        if (terminalError != null) {
            return RegistrationResendResult.failure(terminalError);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (!pending.getExpiresAt().isAfter(now)) {
            // Trả result thay vì ném exception trong transaction để trạng thái EXPIRED được commit.
            pending.expire(now);
            pendingRegistrationRepository.saveAndFlush(pending);
            return RegistrationResendResult.failure(ErrorCode.AUTH_REGISTRATION_EXPIRED);
        }
        if (pending.getResendAvailableAt().isAfter(now)) {
            return RegistrationResendResult.failure(ErrorCode.AUTH_OTP_RESEND_TOO_SOON);
        }

        String rawOtp = otpGenerator.generate();
        LocalDateTime otpExpiresAt = min(now.plus(properties.getOtpExpiration()), pending.getExpiresAt());
        LocalDateTime resendAvailableAt = min(now.plus(properties.getResendCooldown()), pending.getExpiresAt());

        // Hash mới và otp_version mới vô hiệu OTP cũ ngay khi transaction commit; flow token không rotate.
        pending.resend(authHmacService.hashOtp(rawOtp), otpExpiresAt, resendAvailableAt);
        pendingRegistrationRepository.saveAndFlush(pending);

        return RegistrationResendResult.success(new RegistrationOtpIssuance(
                pending.getId(),
                pending.getOtpVersion(),
                pending.getRegistrationType(),
                pending.getIdentifierNormalized(),
                mask(pending),
                rawOtp,
                otpExpiresAt,
                resendAvailableAt,
                pending.getExpiresAt()
        ));
    }

    @Transactional
    public RegistrationStatusResponse status(String rawFlowToken) {
        PendingRegistration pending = findForUpdate(rawFlowToken);
        LocalDateTime now = LocalDateTime.now(clock);
        if (pending.getStatus() == OtpChallengeStatus.PENDING && !pending.getExpiresAt().isAfter(now)) {
            pending.expire(now);
            pendingRegistrationRepository.saveAndFlush(pending);
        }
        return toStatusResponse(pending, now);
    }

    @Transactional
    public CancelRegistrationResponse cancel(String rawFlowToken) {
        PendingRegistration pending = findForUpdate(rawFlowToken);
        LocalDateTime now = LocalDateTime.now(clock);

        if (pending.getStatus() == OtpChallengeStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_ALREADY_COMPLETED);
        }
        if (pending.getStatus() == OtpChallengeStatus.CANCELLED) {
            return cancelResponse(pending);
        }
        if (pending.getStatus() == OtpChallengeStatus.EXPIRED) {
            return cancelResponse(pending);
        }

        if (!pending.getExpiresAt().isAfter(now)) {
            pending.expire(now);
        } else {
            pending.cancel(now);
        }
        pendingRegistrationRepository.saveAndFlush(pending);
        return cancelResponse(pending);
    }

    private PendingRegistration findForUpdate(String rawFlowToken) {
        String flowTokenHash = authHmacService.hashFlowToken(rawFlowToken);
        return pendingRegistrationRepository.findByFlowTokenHashForUpdate(flowTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID));
    }

    private RegistrationStatusResponse toStatusResponse(PendingRegistration pending, LocalDateTime now) {
        boolean active = pending.getStatus() == OtpChallengeStatus.PENDING;
        Integer remainingAttempts = active
                ? Math.max(0, properties.getMaxOtpAttempts() - pending.getFailedAttempts())
                : null;
        boolean canResend = active
                && !pending.getResendAvailableAt().isAfter(now)
                && pending.getExpiresAt().isAfter(now);

        return new RegistrationStatusResponse(
                pending.getStatus(),
                pending.getRegistrationType(),
                mask(pending),
                active ? pending.getOtpExpiresAt() : null,
                active ? pending.getResendAvailableAt() : null,
                pending.getExpiresAt(),
                pending.getResendCount(),
                pending.getDeliveryStatus(),
                canResend,
                remainingAttempts,
                nextStep(pending.getStatus())
        );
    }

    private String mask(PendingRegistration pending) {
        if (pending.getIdentifierNormalized() == null) {
            return null;
        }
        IdentifierType identifierType = pending.getRegistrationType() == RegistrationType.EMAIL
                ? IdentifierType.EMAIL
                : IdentifierType.PHONE_NUMBER;
        return identifierMasker.mask(new NormalizedIdentifier(identifierType, pending.getIdentifierNormalized()));
    }

    private String nextStep(OtpChallengeStatus status) {
        return switch (status) {
            case PENDING -> RegistrationStatusResponse.NEXT_STEP_VERIFY_OTP;
            case COMPLETED -> RegistrationStatusResponse.NEXT_STEP_REGISTRATION_COMPLETED;
            case CANCELLED, EXPIRED -> RegistrationStatusResponse.NEXT_STEP_START_NEW_REGISTRATION;
        };
    }

    private CancelRegistrationResponse cancelResponse(PendingRegistration pending) {
        String message = pending.getStatus() == OtpChallengeStatus.CANCELLED
                ? "Đã hủy đăng ký"
                : "Đăng ký đã hết hạn";
        return new CancelRegistrationResponse(pending.getStatus(), pending.getTerminalAt(), message);
    }

    private ErrorCode terminalError(OtpChallengeStatus status) {
        return switch (status) {
            case PENDING -> null;
            case COMPLETED -> ErrorCode.AUTH_REGISTRATION_ALREADY_COMPLETED;
            case CANCELLED -> ErrorCode.AUTH_REGISTRATION_CANCELLED;
            case EXPIRED -> ErrorCode.AUTH_REGISTRATION_EXPIRED;
        };
    }

    private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }
}
