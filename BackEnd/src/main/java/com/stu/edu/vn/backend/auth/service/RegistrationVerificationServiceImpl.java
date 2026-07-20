package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Validate request và chuyển kết quả transaction thành response hoặc lỗi nghiệp vụ an toàn. */
@Service
@RequiredArgsConstructor
public class RegistrationVerificationServiceImpl implements RegistrationVerificationService {

    private final RegistrationVerificationTransactionService transactionService;
    private final AuthRegistrationProperties properties;

    @Override
    public VerifyRegistrationResponse verify(VerifyRegistrationRequest request, String ipAddress) {
        validate(request);

        RegistrationVerificationResult result;
        try {
            result = transactionService.verify(request, ipAddress);
        } catch (RegistrationIdentifierConflictException exception) {
            ErrorCode code = exception.getRegistrationType() == RegistrationType.EMAIL
                    ? ErrorCode.AUTH_EMAIL_ALREADY_EXISTS
                    : ErrorCode.AUTH_PHONE_ALREADY_EXISTS;
            throw new BusinessException(code);
        } catch (DataIntegrityViolationException exception) {
            // Lỗi profile, Refresh Token hoặc completion rollback toàn transaction và không lộ constraint.
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_CREATION_FAILED);
        }

        if (!result.successful()) {
            throw new BusinessException(result.errorCode());
        }
        return result.response();
    }

    private void validate(VerifyRegistrationRequest request) {
        if (request == null
                || request.registrationFlowToken() == null
                || request.registrationFlowToken().isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID);
        }
        if (request.code() == null
                || !request.code().matches("[0-9]{" + properties.getOtpLength() + "}")) {
            throw new BusinessException(ErrorCode.AUTH_OTP_INVALID);
        }
    }
}
