package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryOutcome;
import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.delivery.RegistrationOtpSender;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.support.EmailMasker;
import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Điều phối validate → commit pending → gửi OTP → ghi delivery status. */
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationTransactionService transactionService;
    private final RegistrationDeliveryStatusService deliveryStatusService;
    private final RegistrationOtpSender otpSender;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final EmailMasker identifierMasker;

    @Override
    public RegisterResponse start(RegisterRequest request) {
        validateRequest(request);
        NormalizedEmail identifier = EmailNormalizer.normalize(request.email());

        RegistrationCreation creation;
        try {
            // Khi proxy transaction trả về thì insert đã commit; unique key là lớp bảo vệ race cuối cùng.
            creation = transactionService.create(identifier, request.password());
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_ALREADY_PENDING);
        }

        if (!creation.resumed()) {
            OtpDeliveryResult deliveryResult = deliver(creation);
            recordDeliveryBestEffort(creation.flowTokenHash(), deliveryResult);
            if (deliveryResult.outcome() != OtpDeliveryOutcome.SENT) {
                throw new BusinessException(ErrorCode.AUTH_OTP_DELIVERY_FAILED);
            }
        }

        return new RegisterResponse(
                creation.rawFlowToken(),
                OtpChallengeStatus.PENDING,
                identifierMasker.mask(identifier),
                creation.otpExpiresAt(),
                creation.resendAvailableAt(),
                creation.pendingExpiresAt(),
                creation.resumed()
        );
    }

    private OtpDeliveryResult deliver(RegistrationCreation creation) {
        try {
            return otpSender.send(creation.type(), creation.normalizedIdentifier(), creation.rawOtp());
        } catch (RuntimeException exception) {
            // Không lưu exception message/payload provider; kết quả không chắc chắn được đánh dấu UNKNOWN.
            return OtpDeliveryResult.unknown();
        }
    }

    /** Không báo gửi thất bại nếu provider đã nhận OTP nhưng bước ghi trạng thái audit gặp lỗi riêng. */
    private void recordDeliveryBestEffort(String flowTokenHash, OtpDeliveryResult result) {
        try {
            deliveryStatusService.record(flowTokenHash, result);
        } catch (RuntimeException exception) {
            if (result.outcome() != OtpDeliveryOutcome.SENT) throw exception;
        }
    }

    private void validateRequest(RegisterRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_IDENTIFIER_INVALID);
        }
        if (!passwordPolicyValidator.isValid(request.password())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        }
        if (request.confirmPassword() == null
                || request.confirmPassword().isBlank()
                || !request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_CONFIRMATION_MISMATCH);
        }
    }
}
