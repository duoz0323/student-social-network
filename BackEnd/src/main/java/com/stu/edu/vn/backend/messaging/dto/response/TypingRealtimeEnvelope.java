package com.stu.edu.vn.backend.messaging.dto.response;

import com.stu.edu.vn.backend.messaging.enums.MessagingRealtimeEventType;
import java.time.LocalDateTime;

/** Typing không có unreadCount vì không thay đổi trạng thái message đã lưu. */
public record TypingRealtimeEnvelope(int schemaVersion, String eventId,
        MessagingRealtimeEventType eventType, LocalDateTime occurredAt, TypingRealtimeData data) { }
