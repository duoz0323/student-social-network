package com.stu.edu.vn.backend.admin.notification.repository;

import com.stu.edu.vn.backend.admin.notification.entity.AdminNotification;
import com.stu.edu.vn.backend.admin.notification.repository.projection.AdminNotificationProjection;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Query của module luôn áp dụng cùng visibility policy dựa trên RBAC hiện tại. */
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    String VISIBLE_PERMISSION = """
            (n.required_permission_code IS NULL OR EXISTS (
                SELECT 1 FROM admin_roles ar
                JOIN roles r ON r.id = ar.role_id
                LEFT JOIN role_permissions rp ON rp.role_id = r.id
                LEFT JOIN permissions p ON p.id = rp.permission_id
                WHERE ar.admin_id = n.recipient_admin_id
                  AND (r.code = 'SUPER_ADMIN' OR p.code = n.required_permission_code)
            ))
            """;

    @Query(value = """
            SELECT DISTINCT u.id
            FROM users u
            JOIN admin_roles ar ON ar.admin_id = u.id
            JOIN roles r ON r.id = ar.role_id
            LEFT JOIN role_permissions rp ON rp.role_id = r.id
            LEFT JOIN permissions p ON p.id = rp.permission_id
            WHERE u.role = 'ADMIN' AND u.status = 'ACTIVE'
              AND (r.code = 'SUPER_ADMIN' OR p.code IN (:permissionCodes))
            """, nativeQuery = true)
    List<Long> findActiveRecipientIdsByAnyPermission(
            @Param("permissionCodes") Collection<String> permissionCodes);

    @Query(value = """
            SELECT DISTINCT u.id
            FROM users u
            JOIN admin_roles ar ON ar.admin_id = u.id
            WHERE u.role = 'ADMIN' AND u.status = 'ACTIVE' AND ar.role_id = :roleId
            """, nativeQuery = true)
    List<Long> findActiveRecipientIdsByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO admin_notifications (
                recipient_admin_id, actor_admin_id, type, title, message,
                required_permission_code, reference_type, reference_id, event_key
            ) VALUES (
                :recipientId, :actorId, :type, :title, :message,
                :permissionCode, :referenceType, :referenceId, :eventKey
            )
            """, nativeQuery = true)
    int insertIgnore(
            @Param("recipientId") Long recipientId,
            @Param("actorId") Long actorId,
            @Param("type") String type,
            @Param("title") String title,
            @Param("message") String message,
            @Param("permissionCode") String permissionCode,
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId,
            @Param("eventKey") String eventKey);

    @Query(value = """
            SELECT id FROM admin_notifications
            WHERE recipient_admin_id = :recipientId AND event_key = :eventKey
            """, nativeQuery = true)
    Optional<Long> findIdByRecipientAndEventKey(
            @Param("recipientId") Long recipientId,
            @Param("eventKey") String eventKey);

    @Query(value = """
            SELECT n.id notificationId, n.type type, n.title title, n.message message,
                   n.required_permission_code requiredPermissionCode,
                   n.reference_type referenceType, n.reference_id referenceId,
                   n.read_at readAt, n.created_at createdAt
            FROM admin_notifications n
            JOIN users u ON u.id = n.recipient_admin_id
            WHERE n.recipient_admin_id = :recipientId
              AND u.role = 'ADMIN' AND u.status = 'ACTIVE'
              AND n.deleted_at IS NULL
              AND """ + VISIBLE_PERMISSION + """
              AND (:cursorCreatedAt IS NULL OR n.created_at < :cursorCreatedAt
                   OR (n.created_at = :cursorCreatedAt AND n.id < :cursorId))
            ORDER BY n.created_at DESC, n.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AdminNotificationProjection> findVisiblePage(
            @Param("recipientId") Long recipientId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit);

    @Query(value = """
            SELECT n.id notificationId, n.type type, n.title title, n.message message,
                   n.required_permission_code requiredPermissionCode,
                   n.reference_type referenceType, n.reference_id referenceId,
                   n.read_at readAt, n.created_at createdAt
            FROM admin_notifications n
            JOIN users u ON u.id = n.recipient_admin_id
            WHERE n.id = :notificationId AND n.recipient_admin_id = :recipientId
              AND u.role = 'ADMIN' AND u.status = 'ACTIVE'
              AND n.deleted_at IS NULL AND """ + VISIBLE_PERMISSION,
            nativeQuery = true)
    Optional<AdminNotificationProjection> findVisibleProjection(
            @Param("notificationId") Long notificationId,
            @Param("recipientId") Long recipientId);

    @Query(value = """
            SELECT COUNT(*) FROM admin_notifications n
            JOIN users u ON u.id = n.recipient_admin_id
            WHERE n.recipient_admin_id = :recipientId
              AND u.role = 'ADMIN' AND u.status = 'ACTIVE'
              AND n.deleted_at IS NULL AND n.read_at IS NULL
              AND """ + VISIBLE_PERMISSION, nativeQuery = true)
    long countVisibleUnread(@Param("recipientId") Long recipientId);

    @Modifying
    @Query(value = """
            UPDATE admin_notifications n
            SET n.read_at = :readAt
            WHERE n.recipient_admin_id = :recipientId
              AND n.deleted_at IS NULL AND n.read_at IS NULL
              AND """ + VISIBLE_PERMISSION, nativeQuery = true)
    int markAllVisibleRead(@Param("recipientId") Long recipientId, @Param("readAt") LocalDateTime readAt);

    Optional<AdminNotification> findByIdAndRecipientAdmin_IdAndDeletedAtIsNull(Long id, Long recipientId);
}
