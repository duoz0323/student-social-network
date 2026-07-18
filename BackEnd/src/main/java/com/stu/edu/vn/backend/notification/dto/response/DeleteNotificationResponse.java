package com.stu.edu.vn.backend.notification.dto.response;

/**
 * Kết quả ẩn một thông báo khỏi hộp thông báo cá nhân.
 */
public record DeleteNotificationResponse(
        Long notificationId,
        boolean deleted
) {
}
