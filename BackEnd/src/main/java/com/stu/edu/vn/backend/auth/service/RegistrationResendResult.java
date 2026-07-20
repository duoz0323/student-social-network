package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.common.exception.ErrorCode;

/** Cho phép commit trạng thái EXPIRED trước khi lớp ngoài transaction phát lỗi nghiệp vụ. */
record RegistrationResendResult(RegistrationOtpIssuance issuance, ErrorCode errorCode) {

    static RegistrationResendResult success(RegistrationOtpIssuance issuance) {
        return new RegistrationResendResult(issuance, null);
    }

    static RegistrationResendResult failure(ErrorCode errorCode) {
        return new RegistrationResendResult(null, errorCode);
    }

    boolean successful() {
        return issuance != null;
    }
}
