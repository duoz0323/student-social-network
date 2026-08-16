package com.stu.edu.vn.backend.messaging.projection;

import java.time.LocalDateTime;

/** Projection gộp Inbox trong một query, gồm profile công khai, last message và unread. */
public interface ConversationListProjection {
    Long getConversationId();
    Long getOtherUserId();
    String getDisplayName();
    String getAvatarUrl();
    Long getLastMessageId();
    Long getLastMessageSenderId();
    String getLastMessageType();
    String getLastMessageContent();
    LocalDateTime getLastMessageAt();
    Long getUnreadCount();
}
