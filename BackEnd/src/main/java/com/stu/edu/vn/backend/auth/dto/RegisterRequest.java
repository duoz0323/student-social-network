package com.stu.edu.vn.backend.auth.dto;

/** Request bắt đầu đăng ký local; Service trả mã lỗi Auth cụ thể cho validation nghiệp vụ. */
public record RegisterRequest(String identifier, String password, String confirmPassword) {
}
