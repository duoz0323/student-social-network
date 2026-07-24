package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;

/**
 * Dữ liệu tài khoản tối thiểu hiển thị trong danh sách quản trị người dùng.
 */
public record AdminUserListItemResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        String email,
        UserStatus status,
        boolean profileCompleted,
        LocalDateTime createdAt
) {
}
