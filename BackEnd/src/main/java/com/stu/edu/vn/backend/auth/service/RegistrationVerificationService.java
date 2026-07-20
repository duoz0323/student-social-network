package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;

/** Use case xác minh OTP và tạo tài khoản thật. */
public interface RegistrationVerificationService {

    VerifyRegistrationResponse verify(VerifyRegistrationRequest request, String ipAddress);
}
