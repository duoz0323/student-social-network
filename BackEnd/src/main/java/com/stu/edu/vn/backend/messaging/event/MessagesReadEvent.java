package com.stu.edu.vn.backend.messaging.event;
/** Domain event chỉ phát khi marker đọc thực sự tiến lên. */
public record MessagesReadEvent(Long conversationId, Long readerId, Long lastReadMessageId) { }
