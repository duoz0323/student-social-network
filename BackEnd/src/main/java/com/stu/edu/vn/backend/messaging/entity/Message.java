package com.stu.edu.vn.backend.messaging.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.messaging.enums.MessageType;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Tin nhắn bất biến về sender, conversation và content trong phạm vi 1B. */
@Entity
@Table(name = "messages", uniqueConstraints = @UniqueConstraint(
        name = "uq_messages_sender_client_message", columnNames = {"sender_id", "client_message_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "client_message_id", nullable = false, length = 36, columnDefinition = "char(36)")
    private String clientMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private MessageType type;

    @Column(name = "content", length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_post_id")
    private Post sharedPost;

    @Column(name = "payload_fingerprint", nullable = false, length = 64, columnDefinition = "char(64)")
    private String payloadFingerprint;

    public Message(Conversation conversation, User sender, String clientMessageId, String content) {
        this(conversation, sender, clientMessageId, MessageType.TEXT, content, null);
    }

    public Message(Conversation conversation, User sender, String clientMessageId, MessageType type,
                   String content, String payloadFingerprint) {
        this(conversation, sender, clientMessageId, type, content, null, payloadFingerprint);
    }

    public Message(Conversation conversation, User sender, String clientMessageId, MessageType type,
                   String content, Post sharedPost, String payloadFingerprint) {
        this.conversation = conversation;
        this.sender = sender;
        this.clientMessageId = clientMessageId;
        this.type = type;
        this.content = content;
        this.sharedPost = sharedPost;
        this.payloadFingerprint = payloadFingerprint == null
                ? com.stu.edu.vn.backend.messaging.support.MessagePayloadFingerprint.text(conversation.getId(), content)
                : payloadFingerprint;
    }
}
