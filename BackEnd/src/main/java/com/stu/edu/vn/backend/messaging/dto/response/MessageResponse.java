package com.stu.edu.vn.backend.messaging.dto.response;

import com.stu.edu.vn.backend.messaging.enums.MessageType;
import java.time.LocalDateTime;

import java.util.List;
/** Response message không chứa Entity hoặc dữ liệu ngoài contract. */
public record MessageResponse(Long messageId, Long conversationId, Long senderId,
                              String clientMessageId, MessageType type, String content,
                              List<MessageAttachmentResponse> attachments, LocalDateTime createdAt) {
    public MessageResponse(Long messageId, Long conversationId, Long senderId, String clientMessageId,
                           MessageType type, String content, LocalDateTime createdAt) {
        this(messageId, conversationId, senderId, clientMessageId, type, content, List.of(), createdAt);
    }
}
