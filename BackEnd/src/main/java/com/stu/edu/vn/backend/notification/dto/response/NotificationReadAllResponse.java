package com.stu.edu.vn.backend.notification.dto.response;

/**
 * Kết quả đánh dấu toàn bộ thông báo chưa đọc của current user.
 */
public record NotificationReadAllResponse(
        int updatedCount
) {
}
