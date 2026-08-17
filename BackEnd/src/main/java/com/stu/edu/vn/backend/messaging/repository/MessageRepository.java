package com.stu.edu.vn.backend.messaging.repository;

import com.stu.edu.vn.backend.messaging.entity.Message;
import jakarta.persistence.LockModeType;
import com.stu.edu.vn.backend.messaging.projection.MessageRealtimeProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Repository message dùng keyset ID và idempotency key của sender. */
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findBySenderIdAndClientMessageId(Long senderId, String clientMessageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select message from Message message where message.sender.id = :senderId and message.clientMessageId = :clientMessageId")
    Optional<Message> findBySenderAndClientMessageIdForUpdate(@Param("senderId") Long senderId,
                                                              @Param("clientMessageId") String clientMessageId);

    @Query(value = "SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY id DESC LIMIT :fetchLimit",
            nativeQuery = true)
    List<Message> findFirstPage(@Param("conversationId") Long conversationId,
                                @Param("fetchLimit") int fetchLimit);

    @Query(value = "SELECT * FROM messages WHERE conversation_id = :conversationId AND id < :cursorId ORDER BY id DESC LIMIT :fetchLimit",
            nativeQuery = true)
    List<Message> findAfter(@Param("conversationId") Long conversationId,
                            @Param("cursorId") Long cursorId,
                            @Param("fetchLimit") int fetchLimit);

    @Query(value = """
            SELECT COUNT(*) FROM messages m
            JOIN conversation_members cm ON cm.conversation_id = m.conversation_id AND cm.user_id = :userId
            JOIN conversations c ON c.id = m.conversation_id
            JOIN users low_user ON low_user.id = c.participant_low_id
            JOIN users high_user ON high_user.id = c.participant_high_id
            WHERE m.sender_id <> :userId AND m.id > COALESCE(cm.last_read_message_id, 0)
              AND c.last_message_id IS NOT NULL
              AND low_user.account_type = 'NORMAL' AND high_user.account_type = 'NORMAL'
              AND NOT EXISTS (
                SELECT 1 FROM user_blocks ub
                WHERE (ub.blocker_id = c.participant_low_id AND ub.blocked_id = c.participant_high_id)
                   OR (ub.blocker_id = c.participant_high_id AND ub.blocked_id = c.participant_low_id)
              )
            """, nativeQuery = true)
    long countUnread(@Param("userId") Long userId);

    @Query(value = """
            SELECT m.id AS messageId, m.conversation_id AS conversationId,
                   m.sender_id AS senderId, m.client_message_id AS clientMessageId,
                   m.type AS type, m.content AS content, m.shared_post_id AS sharedPostId,
                   m.created_at AS createdAt,
                   c.participant_low_id AS participantLowId,
                   c.participant_high_id AS participantHighId
            FROM messages m
            JOIN conversations c ON c.id = m.conversation_id
            JOIN users low_user ON low_user.id = c.participant_low_id
            JOIN user_profiles low_profile ON low_profile.user_id = low_user.id
            JOIN users high_user ON high_user.id = c.participant_high_id
            JOIN user_profiles high_profile ON high_profile.user_id = high_user.id
            WHERE m.id = :messageId
              AND low_user.role = 'USER' AND low_user.status = 'ACTIVE'
              AND high_user.role = 'USER' AND high_user.status = 'ACTIVE'
              AND low_user.account_type = 'NORMAL' AND high_user.account_type = 'NORMAL'
              AND low_profile.profile_completed_at IS NOT NULL
              AND high_profile.profile_completed_at IS NOT NULL
              AND NOT EXISTS (
                SELECT 1 FROM user_blocks ub
                WHERE (ub.blocker_id = c.participant_low_id AND ub.blocked_id = c.participant_high_id)
                   OR (ub.blocker_id = c.participant_high_id AND ub.blocked_id = c.participant_low_id)
              )
            """, nativeQuery = true)
    Optional<MessageRealtimeProjection> findVisibleMessageForRealtime(@Param("messageId") Long messageId);
}
