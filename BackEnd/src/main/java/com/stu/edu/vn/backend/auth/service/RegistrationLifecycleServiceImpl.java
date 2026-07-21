package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryOutcome;
import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.delivery.RegistrationOtpSender;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.RegistrationStatusResponse;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationResponse;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Điều phối commit OTP version mới, gửi provider ngoài transaction và ghi delivery ngắn. */
@Service
@RequiredArgsConstructor
public class RegistrationLifecycleServiceImpl implements RegistrationLifecycleService {

    private final RegistrationLifecycleTransactionService transactionService;
    private final RegistrationDeliveryStatusService deliveryStatusService;
    private final RegistrationOtpSender otpSender;

    @Override
    public ResendRegistrationResponse resend(ResendRegistrationRequest request) {
        String rawFlowToken = requireFlowToken(request == null ? null : request.registrationFlowToken());
        RegistrationResendResult result = transactionService.issueNewOtp(rawFlowToken);
        if (!result.successful()) {
            throw new BusinessException(result.errorCode());
        }

        RegistrationOtpIssuance issuance = result.issuance();
        OtpDeliveryResult deliveryResult = deliver(issuance);
        recordDeliveryBestEffort(issuance, deliveryResult);
        if (deliveryResult.outcome() != OtpDeliveryOutcome.SENT) {
            // OTP version mới vẫn là version hiện hành; tuyệt đối không rollback về OTP cũ.
            throw new BusinessException(ErrorCode.AUTH_OTP_DELIVERY_FAILED);
        }

        return new ResendRegistrationResponse(
                OtpChallengeStatus.PENDING,
                issuance.maskedIdentifier(),
                issuance.otpExpiresAt(),
                issuance.resendAvailableAt(),
                issuance.pendingExpiresAt(),
                "Đã phát hành OTP mới"
        );
    }

    @Override
    public RegistrationStatusResponse status(String registrationFlowToken) {
        return transactionService.status(requireFlowToken(registrationFlowToken));
    }

    @Override
    public CancelRegistrationResponse cancel(CancelRegistrationRequest request) {
        return transactionService.cancel(requireFlowToken(
                request == null ? null : request.registrationFlowToken()
        ));
    }

    private OtpDeliveryResult deliver(RegistrationOtpIssuance issuance) {
        try {
            return otpSender.send(issuance.type(), issuance.normalizedIdentifier(), issuance.rawOtp());
        } catch (RuntimeException exception) {
            // Không lưu hoặc trả message kỹ thuật của provider; timeout được xem là UNKNOWN.
            return OtpDeliveryResult.unknown();
        }
    }

    /** Provider đã nhận OTP thì lỗi ghi audit sau đó không được đổi response thành gửi thất bại. */
    private void recordDeliveryBestEffort(RegistrationOtpIssuance issuance, OtpDeliveryResult result) {
        try {
            deliveryStatusService.record(issuance.pendingId(), issuance.otpVersion(), result);
        } catch (RuntimeException exception) {
            if (result.outcome() != OtpDeliveryOutcome.SENT) throw exception;
        }
    }

    private String requireFlowToken(String rawFlowToken) {
        if (rawFlowToken == null || rawFlowToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);
        }
        return rawFlowToken;
    }
}
