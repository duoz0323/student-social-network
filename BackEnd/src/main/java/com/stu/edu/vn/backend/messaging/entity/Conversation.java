package com.stu.edu.vn.backend.messaging.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Conversation trực tiếp được chuẩn hóa theo cặp participant low/high. */
@Entity
@Table(name = "conversations", uniqueConstraints = @UniqueConstraint(
        name = "uq_conversations_participant_pair", columnNames = {"participant_low_id", "participant_high_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_low_id", nullable = false)
    private User participantLow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_high_id", nullable = false)
    private User participantHigh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    @Setter
    private Message lastMessage;

    @Column(name = "last_message_at")
    @Setter
    private LocalDateTime lastMessageAt;

    public Conversation(User participantLow, User participantHigh) {
        this.participantLow = participantLow;
        this.participantHigh = participantHigh;
    }

    public Long otherParticipantId(Long currentUserId) {
        return participantLow.getId().equals(currentUserId) ? participantHigh.getId() : participantLow.getId();
    }
}
