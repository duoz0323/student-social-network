package com.stu.edu.vn.backend.messaging.repository;

import com.stu.edu.vn.backend.messaging.entity.Conversation;
import com.stu.edu.vn.backend.messaging.projection.ConversationListProjection;
import com.stu.edu.vn.backend.messaging.projection.TypingTargetProjection;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Truy vấn conversation theo cặp chuẩn hóa và Inbox keyset không dùng COUNT. */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByParticipantLowIdAndParticipantHighId(Long lowId, Long highId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select conversation from Conversation conversation where conversation.participantLow.id = :lowId "
            + "and conversation.participantHigh.id = :highId")
    Optional<Conversation> findPairForUpdate(@Param("lowId") Long lowId, @Param("highId") Long highId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select conversation from Conversation conversation where conversation.id = :id")
    Optional<Conversation> findByIdForUpdate(@Param("id") Long id);

    @Query(value = """
            SELECT c.id AS conversationId, :senderId AS senderId,
                   CASE WHEN c.participant_low_id = :senderId
                        THEN c.participant_high_id ELSE c.participant_low_id END AS recipientId
            FROM conversations c
            JOIN conversation_members sender_member
              ON sender_member.conversation_id = c.id AND sender_member.user_id = :senderId
            JOIN conversation_members recipient_member
              ON recipient_member.conversation_id = c.id
             AND recipient_member.user_id = CASE WHEN c.participant_low_id = :senderId
                  THEN c.participant_high_id ELSE c.participant_low_id END
            JOIN users sender_user ON sender_user.id = :senderId
            JOIN user_profiles sender_profile ON sender_profile.user_id = sender_user.id
            JOIN users recipient_user ON recipient_user.id = recipient_member.user_id
            JOIN user_profiles recipient_profile ON recipient_profile.user_id = recipient_user.id
            WHERE c.id = :conversationId
              AND sender_user.role = 'USER' AND sender_user.status = 'ACTIVE'
              AND recipient_user.role = 'USER' AND recipient_user.status = 'ACTIVE'
              AND sender_profile.profile_completed_at IS NOT NULL
              AND recipient_profile.profile_completed_at IS NOT NULL
              AND NOT EXISTS (
                SELECT 1 FROM user_blocks ub
                WHERE (ub.blocker_id = c.participant_low_id AND ub.blocked_id = c.participant_high_id)
                   OR (ub.blocker_id = c.participant_high_id AND ub.blocked_id = c.participant_low_id)
              )
            """, nativeQuery = true)
    Optional<TypingTargetProjection> findTypingTarget(@Param("conversationId") Long conversationId,
                                                       @Param("senderId") Long senderId);

    @Query(value = INBOX_SELECT + INBOX_WHERE + " ORDER BY c.last_message_at DESC, c.id DESC LIMIT :fetchLimit",
            nativeQuery = true)
    List<ConversationListProjection> findInboxFirstPage(@Param("userId") Long userId,
                                                        @Param("fetchLimit") int fetchLimit);

    @Query(value = INBOX_SELECT + INBOX_WHERE + " AND (c.last_message_at < :cursorAt OR "
            + "(c.last_message_at = :cursorAt AND c.id < :cursorId)) "
            + "ORDER BY c.last_message_at DESC, c.id DESC LIMIT :fetchLimit", nativeQuery = true)
    List<ConversationListProjection> findInboxAfter(@Param("userId") Long userId,
                                                    @Param("cursorAt") LocalDateTime cursorAt,
                                                    @Param("cursorId") Long cursorId,
                                                    @Param("fetchLimit") int fetchLimit);

    String INBOX_SELECT = """
            SELECT c.id AS conversationId,
                   CASE WHEN c.participant_low_id = :userId THEN c.participant_high_id ELSE c.participant_low_id END AS otherUserId,
                   up.display_name AS displayName, up.avatar_url AS avatarUrl,
                   lm.id AS lastMessageId, lm.sender_id AS lastMessageSenderId,
                   lm.content AS lastMessageContent, c.last_message_at AS lastMessageAt,
                   (SELECT COUNT(*) FROM messages um
                    WHERE um.conversation_id = c.id AND um.sender_id <> :userId
                      AND um.id > COALESCE(cm.last_read_message_id, 0)) AS unreadCount
            FROM conversations c
            JOIN conversation_members cm ON cm.conversation_id = c.id AND cm.user_id = :userId
            JOIN messages lm ON lm.id = c.last_message_id
            JOIN user_profiles up ON up.user_id = CASE WHEN c.participant_low_id = :userId
                                                       THEN c.participant_high_id ELSE c.participant_low_id END
            """;
    String INBOX_WHERE = """
            WHERE c.last_message_id IS NOT NULL
              AND NOT EXISTS (
                SELECT 1 FROM user_blocks ub
                WHERE (ub.blocker_id = c.participant_low_id AND ub.blocked_id = c.participant_high_id)
                   OR (ub.blocker_id = c.participant_high_id AND ub.blocked_id = c.participant_low_id)
              )
            """;
}
