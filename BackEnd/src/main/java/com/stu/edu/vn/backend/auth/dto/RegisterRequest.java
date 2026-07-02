package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.support.IdentifierNormalizer;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request đăng ký MVP chỉ nhận identifier, password và confirmPassword.
 */
public record RegisterRequest(
        @NotBlank(message = "Email hoặc số điện thoại không được để trống")
        @Size(max = 255, message = "Email hoặc số điện thoại không được vượt quá 255 ký tự")
        String identifier,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Mật khẩu phải có chữ hoa, chữ thường, chữ số và ký tự đặc biệt"
        )
        String password,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword
) {

    @AssertTrue(message = "Email hoặc số điện thoại không hợp lệ")
    public boolean isIdentifierSupported() {
        return IdentifierNormalizer.isSupported(identifier);
    }

    @AssertTrue(message = "Xác nhận mật khẩu không khớp")
    public boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}
