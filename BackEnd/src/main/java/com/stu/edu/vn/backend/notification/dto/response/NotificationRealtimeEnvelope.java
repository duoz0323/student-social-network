package com.stu.edu.vn.backend.notification.dto.response;

import com.stu.edu.vn.backend.notification.enums.NotificationRealtimeEventType;
import java.time.LocalDateTime;

/**
 * Payload realtime tối thiểu, không chứa email, token hoặc dữ liệu xác thực.
 */
public record NotificationRealtimeEnvelope(
        int schemaVersion,
        String eventId,
        NotificationRealtimeEventType eventType,
        LocalDateTime occurredAt,
        Long notificationId,
        NotificationResponse notification,
        long unreadCount
) {
}
