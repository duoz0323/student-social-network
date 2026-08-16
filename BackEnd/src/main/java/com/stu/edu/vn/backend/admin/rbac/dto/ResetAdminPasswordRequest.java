package com.stu.edu.vn.backend.admin.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload cấp lại mật khẩu cho tài khoản quản trị; mật khẩu không xuất hiện trong response hoặc audit log. */
public record ResetAdminPasswordRequest(
        @NotBlank @Size(min = 8, max = 72) String newPassword,
        @NotBlank String confirmPassword
) {
}
