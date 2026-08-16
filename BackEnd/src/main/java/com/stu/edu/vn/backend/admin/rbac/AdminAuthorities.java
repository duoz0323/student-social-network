package com.stu.edu.vn.backend.admin.rbac;

/** Quy ước authority cho vai trò quản trị cấp nghiệp vụ. */
public final class AdminAuthorities {

    public static final String ADMIN_ROLE_PREFIX = "ADMIN_ROLE_";

    private AdminAuthorities() {
    }

    public static String role(AdminRoleCode roleCode) {
        return ADMIN_ROLE_PREFIX + roleCode.name();
    }
}
