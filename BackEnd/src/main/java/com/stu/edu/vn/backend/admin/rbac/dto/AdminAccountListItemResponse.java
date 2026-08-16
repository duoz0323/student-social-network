package com.stu.edu.vn.backend.admin.rbac.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record AdminAccountListItemResponse(
        Long id,
        String email,
        String username,
        String displayName,
        String status,
        Set<String> roles,
        LocalDateTime createdAt
) {
}
