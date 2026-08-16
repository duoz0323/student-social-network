package com.stu.edu.vn.backend.admin.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Yêu cầu đổi mật khẩu của chính quản trị viên đang đăng nhập. */
public record ChangeAdminPasswordRequest(
        @NotBlank @Size(max = 72) String currentPassword,
        @NotBlank @Size(min = 8, max = 72) String newPassword,
        @NotBlank @Size(min = 8, max = 72) String confirmPassword
) {
}
