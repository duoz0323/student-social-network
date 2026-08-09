package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionPostTargetProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionReportTargetProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionUserTargetProjection;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository ghi thao tác ADMIN và cung cấp projection chỉ đọc cho lịch sử quản trị.
 */
public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {

    @Query(value = """
            SELECT aa.id AS actionId, aa.action_type AS actionType,
                   aa.admin_id AS adminId, ap.display_name AS adminDisplayName,
                   ap.avatar_url AS adminAvatarUrl, aa.target_type AS targetType,
                   aa.target_id AS targetId, aa.note AS note, aa.created_at AS createdAt
            FROM admin_actions aa
            JOIN users admin_user ON admin_user.id = aa.admin_id
            LEFT JOIN user_profiles ap ON ap.user_id = admin_user.id
            WHERE (:actionType IS NULL OR aa.action_type = :actionType)
              AND (:targetType IS NULL OR aa.target_type = :targetType)
              AND (:adminId IS NULL OR aa.admin_id = :adminId)
              AND (:fromTime IS NULL OR aa.created_at >= :fromTime)
              AND (:toTime IS NULL OR aa.created_at <= :toTime)
            ORDER BY aa.created_at DESC, aa.id DESC
            """, countQuery = """
            SELECT COUNT(aa.id)
            FROM admin_actions aa
            WHERE (:actionType IS NULL OR aa.action_type = :actionType)
              AND (:targetType IS NULL OR aa.target_type = :targetType)
              AND (:adminId IS NULL OR aa.admin_id = :adminId)
              AND (:fromTime IS NULL OR aa.created_at >= :fromTime)
              AND (:toTime IS NULL OR aa.created_at <= :toTime)
            """, nativeQuery = true)
    Page<AdminActionListProjection> findAdminActions(
            @Param("actionType") String actionType,
            @Param("targetType") String targetType,
            @Param("adminId") Long adminId,
            @Param("fromTime") LocalDateTime from,
            @Param("toTime") LocalDateTime to,
            Pageable pageable
    );

    @Query(value = """
            SELECT aa.id AS actionId, aa.action_type AS actionType,
                   aa.admin_id AS adminId, ap.display_name AS adminDisplayName,
                   ap.avatar_url AS adminAvatarUrl, aa.target_type AS targetType,
                   aa.target_id AS targetId, aa.note AS note, aa.created_at AS createdAt,
                   CAST(aa.old_data AS CHAR) AS oldData,
                   CAST(aa.new_data AS CHAR) AS newData
            FROM admin_actions aa
            JOIN users admin_user ON admin_user.id = aa.admin_id
            LEFT JOIN user_profiles ap ON ap.user_id = admin_user.id
            WHERE aa.id = :actionId
            """, nativeQuery = true)
    Optional<AdminActionDetailProjection> findAdminActionDetail(@Param("actionId") Long actionId);

    @Query(value = """
            SELECT u.id AS targetId, up.display_name AS displayName
            FROM users u
            LEFT JOIN user_profiles up ON up.user_id = u.id
            WHERE u.id IN (:targetIds)
            """, nativeQuery = true)
    List<AdminActionUserTargetProjection> findUserTargets(@Param("targetIds") Collection<Long> targetIds);

    @Query(value = "SELECT p.id AS targetId FROM posts p WHERE p.id IN (:targetIds)", nativeQuery = true)
    List<AdminActionPostTargetProjection> findPostTargets(@Param("targetIds") Collection<Long> targetIds);

    @Query(value = "SELECT h.id AS targetId, h.display_name AS displayName FROM hashtags h WHERE h.id IN (:targetIds)", nativeQuery = true)
    List<AdminActionUserTargetProjection> findHashtagTargets(@Param("targetIds") Collection<Long> targetIds);

    @Query(value = "SELECT r.id AS targetId FROM reports r WHERE r.id IN (:targetIds)", nativeQuery = true)
    List<AdminActionReportTargetProjection> findReportTargets(@Param("targetIds") Collection<Long> targetIds);

    @Query(value = "SELECT mc.id AS targetId FROM moderation_cases mc WHERE mc.id IN (:targetIds)", nativeQuery = true)
    List<AdminActionPostTargetProjection> findModerationCaseTargets(@Param("targetIds") Collection<Long> targetIds);

    @Query(value = "SELECT prc.id AS targetId FROM profile_report_cases prc WHERE prc.id IN (:targetIds)", nativeQuery = true)
    List<AdminActionReportTargetProjection> findProfileReportTargets(
            @Param("targetIds") Collection<Long> targetIds);
}
