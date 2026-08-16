package com.stu.edu.vn.backend.admin.rbac;

/** Các vai trò quản trị nghiệp vụ, tách biệt với users.role = ADMIN. */
public enum AdminRoleCode {
    SUPER_ADMIN,
    USER_MANAGER,
    MODERATOR,
    ADS_MANAGER,
    COLLABORATOR
}
