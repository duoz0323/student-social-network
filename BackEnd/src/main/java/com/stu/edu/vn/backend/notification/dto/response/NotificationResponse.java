package com.stu.edu.vn.backend.notification.dto.response;

import com.stu.edu.vn.backend.notification.enums.NotificationType;
import java.time.LocalDateTime;

/**
 * Response thông báo không làm lộ Entity hoặc dữ liệu xác thực của actor.
 */
public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        NotificationActorResponse actor,
        Long postId,
        Long commentId,
        Long reportId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
