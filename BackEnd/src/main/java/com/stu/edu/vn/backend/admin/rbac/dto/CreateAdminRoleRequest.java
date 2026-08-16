package com.stu.edu.vn.backend.admin.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload tạo vai trò tùy chỉnh; mã kỹ thuật được Backend sinh từ tên hiển thị. */
public record CreateAdminRoleRequest(
        @NotBlank @Size(min = 2, max = 100) String name
) {
}
