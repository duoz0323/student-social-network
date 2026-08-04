package com.stu.edu.vn.backend.messaging.dto.response;
import java.time.LocalDateTime;
/** Payload marker đọc authoritative đọc lại sau commit. */
public record MessagesReadRealtimeData(Long conversationId, Long readerId,
        Long lastReadMessageId, LocalDateTime lastReadAt) { }
