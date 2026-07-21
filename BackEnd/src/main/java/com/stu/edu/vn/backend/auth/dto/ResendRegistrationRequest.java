package com.stu.edu.vn.backend.auth.dto;

/** Request gửi lại OTP; raw flow token chỉ tồn tại trong request và không được ghi log. */
public record ResendRegistrationRequest(String registrationFlowToken) {
}
