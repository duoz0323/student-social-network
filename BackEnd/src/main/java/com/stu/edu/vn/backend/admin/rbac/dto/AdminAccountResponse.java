package com.stu.edu.vn.backend.admin.rbac.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record AdminAccountResponse(
        Long id,
        String email,
        String username,
        String displayName,
        String avatarUrl,
        String bio,
        LocalDate dateOfBirth,
        String status,
        Set<String> roles,
        Set<String> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
