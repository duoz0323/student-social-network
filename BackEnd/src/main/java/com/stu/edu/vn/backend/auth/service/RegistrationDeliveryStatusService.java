package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ghi nhận kết quả delivery trong transaction riêng sau transaction tạo pending đã commit. */
@Service
@RequiredArgsConstructor
public class RegistrationDeliveryStatusService {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final Clock clock;

    @Transactional
    public void record(String flowTokenHash, OtpDeliveryResult result) {
        PendingRegistration pending = pendingRegistrationRepository.findByFlowTokenHashForUpdate(flowTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REGISTRATION_START_FAILED));
        recordResult(pending, result, LocalDateTime.now(clock));
        pendingRegistrationRepository.saveAndFlush(pending);
    }

    /**
     * Ghi delivery đúng OTP version; response provider cũ không được ghi đè resend mới hoặc terminal state.
     */
    @Transactional
    public void record(Long pendingId, int expectedOtpVersion, OtpDeliveryResult result) {
        PendingRegistration pending = pendingRegistrationRepository.findByIdForUpdate(pendingId).orElse(null);
        if (pending == null
                || pending.getStatus() != OtpChallengeStatus.PENDING
                || pending.getOtpVersion() != expectedOtpVersion) {
            return;
        }
        recordResult(pending, result, LocalDateTime.now(clock));
        pendingRegistrationRepository.saveAndFlush(pending);
    }

    private void recordResult(PendingRegistration pending, OtpDeliveryResult result, LocalDateTime attemptedAt) {
        switch (result.outcome()) {
            case SENT -> pending.markDeliverySent(attemptedAt);
            case FAILED -> pending.markDeliveryFailed(attemptedAt, normalizeFailureCode(result.failureCode()));
            case UNKNOWN -> pending.markDeliveryUnknown(attemptedAt);
        }
    }

    private String normalizeFailureCode(String failureCode) {
        if (failureCode == null || !failureCode.matches("[A-Z0-9_]{1,64}")) {
            return "DELIVERY_FAILED";
        }
        return failureCode;
    }
}
