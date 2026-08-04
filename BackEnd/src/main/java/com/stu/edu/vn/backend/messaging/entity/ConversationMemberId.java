package com.stu.edu.vn.backend.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/** Khóa kép bảo đảm mỗi user chỉ có một membership trong conversation. */
@Embeddable
public record ConversationMemberId(
        @Column(name = "conversation_id") Long conversationId,
        @Column(name = "user_id") Long userId
) implements Serializable {
    public ConversationMemberId() { this(null, null); }
}
