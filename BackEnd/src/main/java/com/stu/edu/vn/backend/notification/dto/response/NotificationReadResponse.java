package com.stu.edu.vn.backend.notification.dto.response;

import java.time.LocalDateTime;

/**
 * Kết quả đánh dấu một thông báo đã đọc.
 */
public record NotificationReadResponse(
        Long notificationId,
        LocalDateTime readAt
) {
}
