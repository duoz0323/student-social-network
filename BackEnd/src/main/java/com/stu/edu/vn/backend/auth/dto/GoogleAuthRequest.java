package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request Google Auth chỉ nhận credential và metadata phiên, không nhận danh tính tự khai báo. */
public record GoogleAuthRequest(
        @NotBlank(message = "Google ID Token không được để trống")
        @Size(max = 8192, message = "Google ID Token không hợp lệ")
        @Pattern(regexp = "^\\S+$", message = "Google ID Token không hợp lệ")
        String idToken,
        @Size(max = 100, message = "Device ID không được vượt quá 100 ký tự") String deviceId,
        @Size(max = 500, message = "Device info không được vượt quá 500 ký tự") String deviceInfo
) {
}
