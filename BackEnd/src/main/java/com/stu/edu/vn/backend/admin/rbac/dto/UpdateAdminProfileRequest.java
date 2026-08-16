package com.stu.edu.vn.backend.admin.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Dữ liệu hồ sơ mà quản trị viên được phép tự chỉnh sửa. */
public record UpdateAdminProfileRequest(
        @NotBlank @Size(min = 2, max = 100) String displayName,
        @NotNull LocalDate dateOfBirth,
        @Size(max = 500) String bio
) {
}
