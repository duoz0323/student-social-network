package com.stu.edu.vn.backend.messaging.projection;

/** Participant đích đã qua đầy đủ kiểm tra membership, account, profile và Block. */
public interface TypingTargetProjection {
    Long getConversationId();
    Long getSenderId();
    Long getRecipientId();
}
