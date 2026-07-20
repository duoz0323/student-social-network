package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request chỉ nhận Facebook Access Token và metadata phiên, không nhận danh tính tự khai báo. */
public record FacebookAuthRequest(
        @NotBlank(message = "Facebook Access Token không được để trống")
        @Size(max = 8192, message = "Facebook Access Token không hợp lệ")
        @Pattern(regexp = "^\\S+$", message = "Facebook Access Token không hợp lệ") String accessToken,
        @Size(max = 100) String deviceId,
        @Size(max = 500) String deviceInfo
) { }
