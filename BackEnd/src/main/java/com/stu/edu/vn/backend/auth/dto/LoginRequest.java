package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request đăng nhập local chỉ nhận email và mật khẩu. */
public record LoginRequest(
        @NotBlank(message = "Email không được để trống")
        @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
        String email,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 72, message = "Mật khẩu không được vượt quá 72 ký tự")
        String password,
        @Size(max = 100) String deviceId,
        @Size(max = 500) String deviceInfo
) {
    @AssertTrue(message = "Email không hợp lệ")
    public boolean isEmailSupported() {
        return EmailNormalizer.isValid(email);
    }
}
