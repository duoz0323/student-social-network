package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request làm mới Access Token chỉ nhận Refresh Token thô từ Client.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh Token không được để trống")
        @Size(max = 2048, message = "Refresh Token không hợp lệ")
        @Pattern(regexp = "^\\S+$", message = "Refresh Token không hợp lệ")
        String refreshToken
) {
}
