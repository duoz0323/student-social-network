package com.stu.edu.vn.backend.messaging.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Trạng thái đọc riêng của từng participant trong một conversation. */
@Entity
@Table(name = "conversation_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationMember extends BaseAuditEntity {
    @EmbeddedId
    private ConversationMemberId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private Message lastReadMessage;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    public ConversationMember(Conversation conversation, User user) {
        this.conversation = conversation;
        this.user = user;
        this.id = new ConversationMemberId(conversation.getId(), user.getId());
    }

    public void advanceReadMarker(Message message, LocalDateTime readAt) {
        this.lastReadMessage = message;
        this.lastReadAt = readAt;
    }
}
