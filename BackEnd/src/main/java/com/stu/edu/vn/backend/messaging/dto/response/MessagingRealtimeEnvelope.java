package com.stu.edu.vn.backend.messaging.dto.response;
import com.stu.edu.vn.backend.messaging.enums.MessagingRealtimeEventType;
import java.time.LocalDateTime;
/** Envelope chung; unreadCount được tính riêng cho user nhận. */
public record MessagingRealtimeEnvelope(int schemaVersion, String eventId,
        MessagingRealtimeEventType eventType, LocalDateTime occurredAt, Object data, long unreadCount) { }
