package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Hoàn tất OTP và mật khẩu ban đầu trong cùng một transaction. */
public record CompleteEmailLinkRequest(
        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(max = 72, message = "Mật khẩu mới không hợp lệ")
        String newPassword,
        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        @Size(max = 72, message = "Xác nhận mật khẩu không hợp lệ")
        String confirmPassword
) { }
