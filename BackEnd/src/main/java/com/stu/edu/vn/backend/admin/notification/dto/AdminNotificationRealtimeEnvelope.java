package com.stu.edu.vn.backend.admin.notification.dto;

import java.time.LocalDateTime;

public record AdminNotificationRealtimeEnvelope(
        int schemaVersion,
        String eventId,
        String event,
        LocalDateTime occurredAt,
        AdminNotificationResponse notification,
        long unreadCount
) {}
