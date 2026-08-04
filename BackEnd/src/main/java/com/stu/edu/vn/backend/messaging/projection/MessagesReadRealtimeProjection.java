package com.stu.edu.vn.backend.messaging.projection;

import java.time.LocalDateTime;

/** Marker đọc authoritative sau khi đã kiểm tra account, profile và Block. */
public interface MessagesReadRealtimeProjection {
    Long getConversationId();
    Long getReaderId();
    Long getOtherUserId();
    Long getLastReadMessageId();
    LocalDateTime getLastReadAt();
}
