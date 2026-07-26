package com.stu.edu.vn.backend.notification.repository;

import com.stu.edu.vn.backend.notification.entity.Notification;
import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.notification.repository.projection.NotificationListProjection;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy cập hộp thông báo và xóa side effect theo nguồn nghiệp vụ.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(
            value = """
                    SELECT n.id AS notificationId,
                           n.type AS type,
                           n.actor_id AS actorId,
                           up.display_name AS actorDisplayName,
                           up.avatar_url AS actorAvatarUrl,
                           n.post_id AS postId,
                           n.comment_id AS commentId,
                           n.report_id AS reportId,
                           n.read_at AS readAt,
                           n.created_at AS createdAt
                    FROM notifications n
                    LEFT JOIN user_profiles up ON up.user_id = n.actor_id
                    WHERE n.recipient_id = :recipientId
                      AND n.deleted_at IS NULL
                      AND (n.actor_id IS NULL OR NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :recipientId AND ub.blocked_id = n.actor_id)
                             OR (ub.blocker_id = n.actor_id AND ub.blocked_id = :recipientId)
                      ))
                    ORDER BY n.created_at DESC, n.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(n.id)
                    FROM notifications n
                    WHERE n.recipient_id = :recipientId
                      AND n.deleted_at IS NULL
                      AND (n.actor_id IS NULL OR NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :recipientId AND ub.blocked_id = n.actor_id)
                             OR (ub.blocker_id = n.actor_id AND ub.blocked_id = :recipientId)
                      ))
                    """,
            nativeQuery = true
    )
    Page<NotificationListProjection> findVisibleNotifications(
            @Param("recipientId") Long recipientId,
            Pageable pageable
    );

    long countByRecipient_IdAndReadAtIsNullAndDeletedAtIsNull(Long recipientId);

    Optional<Notification> findByIdAndRecipient_IdAndDeletedAtIsNull(Long id, Long recipientId);

    @Modifying
    @Query("""
            UPDATE Notification notification
            SET notification.readAt = :readAt
            WHERE notification.recipient.id = :recipientId
              AND notification.readAt IS NULL
              AND notification.deletedAt IS NULL
            """)
    int markAllRead(
            @Param("recipientId") Long recipientId,
            @Param("readAt") LocalDateTime readAt
    );

    @Modifying
    @Query("""
            DELETE FROM Notification notification
            WHERE notification.type = :type
              AND notification.actor.id = :actorId
              AND notification.recipient.id = :recipientId
            """)
    int deleteFollowNotification(
            @Param("type") NotificationType type,
            @Param("actorId") Long actorId,
            @Param("recipientId") Long recipientId
    );

    @Modifying
    @Query("""
            DELETE FROM Notification notification
            WHERE notification.type = :type
              AND notification.actor.id = :actorId
              AND notification.post.id = :postId
            """)
    int deletePostLikeNotification(
            @Param("type") NotificationType type,
            @Param("actorId") Long actorId,
            @Param("postId") Long postId
    );

    @Modifying
    @Query("""
            DELETE FROM Notification notification
            WHERE notification.comment.id = :commentId
            """)
    int deleteCommentNotification(@Param("commentId") Long commentId);
}
