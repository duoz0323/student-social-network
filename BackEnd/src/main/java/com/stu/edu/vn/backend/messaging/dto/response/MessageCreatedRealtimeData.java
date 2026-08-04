package com.stu.edu.vn.backend.messaging.dto.response;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import java.time.LocalDateTime;
import java.util.List;
/** Payload message tối thiểu, không chứa profile hoặc dữ liệu xác thực. */
public record MessageCreatedRealtimeData(Long messageId, Long conversationId, Long senderId,
        String clientMessageId, MessageType type, String content,
        List<MessageAttachmentResponse> attachments, LocalDateTime createdAt) {
    public MessageCreatedRealtimeData(Long messageId, Long conversationId, Long senderId, String clientMessageId,
            MessageType type, String content, LocalDateTime createdAt) {
        this(messageId, conversationId, senderId, clientMessageId, type, content, List.of(), createdAt);
    }
}
