package com.stu.edu.vn.backend.notification.dto.response;

/**
 * Số thông báo chưa đọc, không tính các thông báo đã bị ẩn.
 */
public record NotificationUnreadCountResponse(
        long unreadCount
) {
}
