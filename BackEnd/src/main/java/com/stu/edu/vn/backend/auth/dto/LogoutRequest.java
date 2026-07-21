package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request đăng xuất phiên hiện tại, Client gửi Refresh Token của phiên cần thu hồi.
 */
public record LogoutRequest(
        @NotBlank(message = "Refresh Token không được để trống")
        @Size(max = 2048, message = "Refresh Token không hợp lệ")
        @Pattern(regexp = "^\\S+$", message = "Refresh Token không hợp lệ")
        String refreshToken
) {
}
