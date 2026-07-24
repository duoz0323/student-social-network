package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;

/**
 * Dữ liệu tài khoản và hồ sơ an toàn dành cho màn hình chi tiết phía ADMIN.
 */
public record AdminUserDetailResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        String bio,
        String email,
        UserStatus status,
        boolean profileCompleted,
        LocalDateTime profileCompletedAt,
        LocalDateTime blockedAt,
        String blockedReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
