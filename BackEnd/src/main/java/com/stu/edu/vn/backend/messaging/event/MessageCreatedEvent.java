package com.stu.edu.vn.backend.messaging.event;
/** Domain event nhẹ; listener phải đọc lại dữ liệu sau commit. */
public record MessageCreatedEvent(Long messageId, Long conversationId) { }
