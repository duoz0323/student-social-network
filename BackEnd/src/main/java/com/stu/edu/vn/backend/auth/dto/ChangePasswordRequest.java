package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Đổi mật khẩu local hiện tại; không nhận userId từ client. */
public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu hiện tại không được để trống")
        @Size(max = 72, message = "Mật khẩu hiện tại không hợp lệ")
        String currentPassword,
        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(max = 72, message = "Mật khẩu mới không hợp lệ")
        String newPassword,
        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        @Size(max = 72, message = "Xác nhận mật khẩu không hợp lệ")
        String confirmPassword
) { }
