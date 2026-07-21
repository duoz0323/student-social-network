package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.enums.RegistrationType;

/** Exception nội bộ giữ loại identifier để ánh xạ race database mà không lộ constraint SQL. */
class RegistrationIdentifierConflictException extends RuntimeException {

    private final RegistrationType registrationType;

    RegistrationIdentifierConflictException(RegistrationType registrationType, Throwable cause) {
        super(cause);
        this.registrationType = registrationType;
    }

    RegistrationType getRegistrationType() {
        return registrationType;
    }
}
