package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;

/**
 * Trạng thái tài khoản sau thao tác khóa hoặc mở khóa, không chứa dữ liệu xác thực nhạy cảm.
 */
public record AdminUserStatusResponse(
        Long userId,
        UserStatus status,
        LocalDateTime blockedAt,
        String blockedReason,
        LocalDateTime updatedAt
) {
}
