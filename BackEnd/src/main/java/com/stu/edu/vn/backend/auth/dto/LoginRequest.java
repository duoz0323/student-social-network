package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.support.IdentifierNormalizer;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request đăng nhập MVP chỉ nhận identifier và mật khẩu, không hỗ trợ username.
 */
public record LoginRequest(
        @NotBlank(message = "Email hoặc số điện thoại không được để trống")
        @Size(max = 255, message = "Email hoặc số điện thoại không được vượt quá 255 ký tự")
        String identifier,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 72, message = "Mật khẩu không được vượt quá 72 ký tự")
        String password,

        @Size(max = 100, message = "Mã thiết bị không được vượt quá 100 ký tự")
        String deviceId,

        @Size(max = 500, message = "Thông tin thiết bị không được vượt quá 500 ký tự")
        String deviceInfo
) {

    @AssertTrue(message = "Email hoặc số điện thoại không hợp lệ")
    public boolean isIdentifierSupported() {
        // Dùng chung utility nhận diện email/số điện thoại để không phát sinh username trong MVP.
        return IdentifierNormalizer.isSupported(identifier);
    }
}
