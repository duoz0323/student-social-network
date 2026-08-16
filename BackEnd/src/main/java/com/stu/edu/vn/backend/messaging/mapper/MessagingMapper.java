package com.stu.edu.vn.backend.messaging.mapper;

import com.stu.edu.vn.backend.messaging.dto.response.*;
import com.stu.edu.vn.backend.messaging.entity.Message;
import com.stu.edu.vn.backend.messaging.entity.MessageAttachment;
import com.stu.edu.vn.backend.messaging.projection.ConversationListProjection;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import org.springframework.stereotype.Component;
import java.util.List;

/** Mapper thủ công nhỏ giữ toàn bộ lazy access bên trong transaction Service. */
@Component
public class MessagingMapper {
    public ConversationResponse toConversationResponse(ConversationListProjection item) {
        return new ConversationResponse(item.getConversationId(),
                new MessagingUserResponse(item.getOtherUserId(), item.getDisplayName(), item.getAvatarUrl()),
                new LastMessageResponse(item.getLastMessageId(), item.getLastMessageSenderId(),
                        MessageType.valueOf(item.getLastMessageType()), item.getLastMessageContent(),
                        item.getLastMessageAt()),
                item.getUnreadCount() == null ? 0 : item.getUnreadCount());
    }

    public MessageResponse toMessageResponse(Message message) {
        return toMessageResponse(message, List.of());
    }

    public MessageResponse toMessageResponse(Message message, List<MessageAttachment> attachments) {
        return toMessageResponse(message, attachments, null);
    }

    public MessageResponse toMessageResponse(Message message, List<MessageAttachment> attachments,
                                             SharedPostResponse sharedPost) {
        List<MessageAttachmentResponse> metadata = attachments.stream()
                .map(item -> new MessageAttachmentResponse(item.getId(), item.getMediaType(), item.getMimeType(),
                        item.getFileSizeBytes(), item.getWidth(), item.getHeight(), item.getDisplayOrder()))
                .toList();
        return new MessageResponse(message.getId(), message.getConversation().getId(), message.getSender().getId(),
                message.getClientMessageId(), message.getType(), message.getContent(), metadata, sharedPost,
                message.getType() == MessageType.POST_SHARE && sharedPost == null, message.getCreatedAt());
    }
}
