package com.stu.edu.vn.backend.admin.rbac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

/** Payload tạo admin thật, có hồ sơ hoàn tất và ít nhất một role RBAC. */
public record CreateAdminRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String confirmPassword,
        @NotBlank String username,
        @NotBlank @Size(max = 100) String displayName,
        LocalDate dateOfBirth,
        @NotEmpty Set<String> roleCodes
) {
}
