package com.stu.edu.vn.backend.notification.event;

/**
 * Domain event nhẹ, không mang Entity managed qua ranh giới transaction.
 */
public record NotificationCreatedEvent(
        Long notificationId,
        Long recipientId
) {
}
