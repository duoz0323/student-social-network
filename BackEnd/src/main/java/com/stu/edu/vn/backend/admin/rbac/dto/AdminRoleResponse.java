package com.stu.edu.vn.backend.admin.rbac.dto;

import java.util.Set;

public record AdminRoleResponse(
        String code,
        String displayName,
        String description,
        boolean reserved,
        Set<String> permissions
) {
}
