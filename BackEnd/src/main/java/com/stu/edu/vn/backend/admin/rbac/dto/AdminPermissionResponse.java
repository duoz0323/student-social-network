package com.stu.edu.vn.backend.admin.rbac.dto;

/** Permission hiển thị trong ma trận phân quyền; module giúp Frontend nhóm checkbox. */
public record AdminPermissionResponse(String code, String description, String module) {
}
