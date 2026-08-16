package com.stu.edu.vn.backend.admin.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Chỉ cập nhật dữ liệu hồ sơ; role có endpoint riêng để audit rõ ràng. */
public record UpdateAdminRequest(@NotBlank @Size(max = 100) String displayName) {
}
