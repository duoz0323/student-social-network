package com.stu.edu.vn.backend.auth.delivery;

import com.stu.edu.vn.backend.auth.enums.RegistrationType;

/** Adapter gửi OTP; không được chứa logic tạo pending hoặc transaction nghiệp vụ. */
public interface RegistrationOtpSender {

    OtpDeliveryResult send(RegistrationType type, String normalizedIdentifier, String rawOtp);
}
