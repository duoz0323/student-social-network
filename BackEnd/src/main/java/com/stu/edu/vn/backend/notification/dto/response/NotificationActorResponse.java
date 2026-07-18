package com.stu.edu.vn.backend.notification.dto.response;

/**
 * Thông tin hồ sơ công khai tối thiểu của người tạo tương tác.
 */
public record NotificationActorResponse(
        Long userId,
        String displayName,
        String avatarUrl
) {
}
