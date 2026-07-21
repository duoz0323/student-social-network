package com.stu.edu.vn.backend.auth.dto;

/** Request xác minh đăng ký; Service thực hiện validation để trả mã lỗi Auth cụ thể. */
public record VerifyRegistrationRequest(
        String registrationFlowToken,
        String code,
        String deviceId,
        String deviceInfo
) {
}
