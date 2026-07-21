package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryOutcome;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.enums.PasswordRecoveryStatus;
import com.stu.edu.vn.backend.auth.repository.PasswordRecoveryChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transaction ngắn claim/finalize delivery; không bao quanh lời gọi Brevo. */
@Service
public class PasswordRecoveryDeliveryStatusService {
    private final PasswordRecoveryChallengeRepository repository;
    public PasswordRecoveryDeliveryStatusService(PasswordRecoveryChallengeRepository repository) { this.repository = repository; }

    @Transactional
    public boolean claim(Long id, int otpVersion) {
        var challenge = repository.findByIdAndOtpVersion(id, otpVersion).orElse(null);
        if (challenge == null || challenge.getStatus() != PasswordRecoveryStatus.PENDING
                || challenge.getUser() == null || challenge.getDeliveryStatus() != OtpDeliveryStatus.PENDING) return false;
        challenge.markDeliverySending();
        return true;
    }

    @Transactional
    public void finish(Long id, int otpVersion, OtpDeliveryOutcome outcome, String failureCode, int attempts) {
        var challenge = repository.findByIdAndOtpVersion(id, otpVersion).orElse(null);
        if (challenge == null || challenge.getDeliveryStatus() != OtpDeliveryStatus.SENDING) return;
        switch (outcome) {
            case SENT -> challenge.markDeliverySent(attempts);
            case FAILED -> challenge.markDeliveryFailed(failureCode, attempts);
            case UNKNOWN -> challenge.markDeliveryUnknown(attempts);
        }
    }
}
