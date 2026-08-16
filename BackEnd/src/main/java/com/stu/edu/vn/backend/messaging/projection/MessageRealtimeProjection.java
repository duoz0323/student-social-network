package com.stu.edu.vn.backend.messaging.projection;
import java.time.LocalDateTime;
/** Projection đã kiểm tra account/profile/Block cho MESSAGE_CREATED. */
public interface MessageRealtimeProjection {
    Long getMessageId(); Long getConversationId(); Long getSenderId(); String getClientMessageId();
    String getType(); String getContent(); LocalDateTime getCreatedAt();
    Long getSharedPostId();
    Long getParticipantLowId(); Long getParticipantHighId();
}
