package com.stu.edu.vn.backend.auth.dto;

/** Request hủy đăng ký; Service dùng HMAC lookup thay vì lưu raw flow token. */
public record CancelRegistrationRequest(String registrationFlowToken) {
}
