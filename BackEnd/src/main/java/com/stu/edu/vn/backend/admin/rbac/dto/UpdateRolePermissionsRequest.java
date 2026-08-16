package com.stu.edu.vn.backend.admin.rbac.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

/** Gửi snapshot đầy đủ để cập nhật role_permissions nguyên tử, không toggle mơ hồ. */
public record UpdateRolePermissionsRequest(@NotNull Set<String> permissionCodes) {
}
