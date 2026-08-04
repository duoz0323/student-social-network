package com.stu.edu.vn.backend.messaging.repository;

import com.stu.edu.vn.backend.messaging.entity.ConversationMember;
import com.stu.edu.vn.backend.messaging.entity.ConversationMemberId;
import com.stu.edu.vn.backend.messaging.projection.MessagesReadRealtimeProjection;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Repository membership và marker đọc monotonic. */
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {
    boolean existsByIdConversationIdAndIdUserId(Long conversationId, Long userId);

    List<ConversationMember> findAllByIdConversationId(Long conversationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select member from ConversationMember member where member.id.conversationId = :conversationId and member.id.userId = :userId")
    Optional<ConversationMember> findForUpdate(@Param("conversationId") Long conversationId,
                                               @Param("userId") Long userId);

    @Query(value = """
            SELECT cm.conversation_id AS conversationId, cm.user_id AS readerId,
                   CASE WHEN c.participant_low_id = cm.user_id
                        THEN c.participant_high_id ELSE c.participant_low_id END AS otherUserId,
                   cm.last_read_message_id AS lastReadMessageId, cm.last_read_at AS lastReadAt
            FROM conversation_members cm
            JOIN conversations c ON c.id = cm.conversation_id
            JOIN users low_user ON low_user.id = c.participant_low_id
            JOIN user_profiles low_profile ON low_profile.user_id = low_user.id
            JOIN users high_user ON high_user.id = c.participant_high_id
            JOIN user_profiles high_profile ON high_profile.user_id = high_user.id
            WHERE cm.conversation_id = :conversationId AND cm.user_id = :readerId
              AND cm.last_read_message_id IS NOT NULL
              AND low_user.role = 'USER' AND low_user.status = 'ACTIVE'
              AND high_user.role = 'USER' AND high_user.status = 'ACTIVE'
              AND low_profile.profile_completed_at IS NOT NULL
              AND high_profile.profile_completed_at IS NOT NULL
              AND NOT EXISTS (
                SELECT 1 FROM user_blocks ub
                WHERE (ub.blocker_id = c.participant_low_id AND ub.blocked_id = c.participant_high_id)
                   OR (ub.blocker_id = c.participant_high_id AND ub.blocked_id = c.participant_low_id)
              )
            """, nativeQuery = true)
    Optional<MessagesReadRealtimeProjection> findVisibleReadMarkerForRealtime(
            @Param("conversationId") Long conversationId, @Param("readerId") Long readerId);
}
