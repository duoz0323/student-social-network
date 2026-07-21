package com.stu.edu.vn.backend.auth.delivery;

import com.stu.edu.vn.backend.auth.service.PasswordRecoveryDeliveryStatusService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Gửi OTP sau commit và chỉ cập nhật đúng phiên bản để callback cũ không ghi đè trạng thái mới. */
@Component
public class PasswordRecoveryOtpListener {
    private final RegistrationOtpSender sender;
    private final PasswordRecoveryDeliveryStatusService statuses;

    public PasswordRecoveryOtpListener(RegistrationOtpSender sender, PasswordRecoveryDeliveryStatusService statuses) {
        this.sender = sender;
        this.statuses = statuses;
    }

    @Async("passwordRecoveryExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deliver(PasswordRecoveryOtpRequested event) {
        if (!statuses.claim(event.challengeId(), event.otpVersion())) return;
        OtpDeliveryResult result;
        int attempts = 1;
        try {
            result = sender.send(event.channel(), event.destination(), event.rawOtp());
        } catch (RuntimeException exception) {
            statuses.finish(event.challengeId(), event.otpVersion(), OtpDeliveryOutcome.UNKNOWN, null, attempts);
            return;
        }
        if (result.outcome() == OtpDeliveryOutcome.FAILED) {
            attempts++;
            try { result = sender.send(event.channel(), event.destination(), event.rawOtp()); }
            catch (RuntimeException exception) {
                statuses.finish(event.challengeId(), event.otpVersion(), OtpDeliveryOutcome.UNKNOWN, null, attempts);
                return;
            }
        }
        String failureCode = result.outcome() == OtpDeliveryOutcome.FAILED
                ? normalizeFailureCode(result.failureCode()) : null;
        statuses.finish(event.challengeId(), event.otpVersion(), result.outcome(), failureCode, attempts);
    }

    private String normalizeFailureCode(String value) {
        if (value == null || value.isBlank()) return "PROVIDER_FAILED";
        return value.length() <= 64 ? value : value.substring(0, 64);
    }
}
