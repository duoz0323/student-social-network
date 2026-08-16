package com.stu.edu.vn.backend.admin.collaborator.identity;

/** DTO công khai tối thiểu, không để lộ tài khoản Admin điều khiển danh tính. */
public record ManagedSocialIdentityResponse(
        Long userId,
        String username,
        String displayName,
        String avatarUrl,
        String bio,
        boolean managed
) { }
