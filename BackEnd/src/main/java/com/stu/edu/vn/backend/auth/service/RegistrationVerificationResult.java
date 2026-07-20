package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;
import com.stu.edu.vn.backend.common.exception.ErrorCode;

/**
 * Kết quả transaction giúp commit failed_attempts/EXPIRED trước khi tầng ngoài phát sinh BusinessException.
 */
record RegistrationVerificationResult(
        VerifyRegistrationResponse response,
        ErrorCode errorCode
) {

    static RegistrationVerificationResult success(VerifyRegistrationResponse response) {
        return new RegistrationVerificationResult(response, null);
    }

    static RegistrationVerificationResult failure(ErrorCode errorCode) {
        return new RegistrationVerificationResult(null, errorCode);
    }

    boolean successful() {
        return response != null;
    }
}
