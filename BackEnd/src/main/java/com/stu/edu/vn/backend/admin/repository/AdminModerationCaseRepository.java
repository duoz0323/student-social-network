package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseActionProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseReportProjection;
import com.stu.edu.vn.backend.admin.repository.projection.ModerationReasonCountProjection;
import com.stu.edu.vn.backend.report.entity.ModerationCase;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Repository projection cho danh sách/chi tiết Moderation Case; aggregation thực hiện tại MySQL. */
public interface AdminModerationCaseRepository extends Repository<ModerationCase, Long> {

    @Query(value = """
            SELECT mc.id AS caseId, mc.status AS status, mc.post_id AS postId,
                   p.content AS postContentPreview, p.author_id AS postAuthorId,
                   ap.display_name AS postAuthorDisplayName, mc.report_count AS reportCount,
                   (SELECT COUNT(DISTINCT r.reporter_id) FROM reports r
                    WHERE r.moderation_case_id = mc.id) AS distinctReporterCount,
                   mc.resolved_by AS resolvedBy, rp.display_name AS resolvedByDisplayName,
                   mc.first_reported_at AS firstReportedAt,
                   mc.latest_reported_at AS latestReportedAt, mc.resolved_at AS resolvedAt
            FROM moderation_cases mc
            JOIN posts p ON p.id = mc.post_id
            LEFT JOIN user_profiles ap ON ap.user_id = p.author_id
            LEFT JOIN user_profiles rp ON rp.user_id = mc.resolved_by
            WHERE (:status IS NULL OR mc.status = :status)
              AND (:postId IS NULL OR mc.post_id = :postId)
              AND (:fromTime IS NULL OR mc.latest_reported_at >= :fromTime)
              AND (:toTime IS NULL OR mc.latest_reported_at <= :toTime)
              AND (:keyword IS NULL
                   OR LOWER(p.content) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(ap.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
              AND (:reason IS NULL OR EXISTS (
                   SELECT 1 FROM reports rr
                   WHERE rr.moderation_case_id = mc.id AND rr.reason = :reason))
            ORDER BY mc.latest_reported_at DESC, mc.id DESC
            """, countQuery = """
            SELECT COUNT(mc.id)
            FROM moderation_cases mc
            JOIN posts p ON p.id = mc.post_id
            LEFT JOIN user_profiles ap ON ap.user_id = p.author_id
            WHERE (:status IS NULL OR mc.status = :status)
              AND (:postId IS NULL OR mc.post_id = :postId)
              AND (:fromTime IS NULL OR mc.latest_reported_at >= :fromTime)
              AND (:toTime IS NULL OR mc.latest_reported_at <= :toTime)
              AND (:keyword IS NULL
                   OR LOWER(p.content) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(ap.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
              AND (:reason IS NULL OR EXISTS (
                   SELECT 1 FROM reports rr
                   WHERE rr.moderation_case_id = mc.id AND rr.reason = :reason))
            """, nativeQuery = true)
    Page<AdminModerationCaseListProjection> findCases(
            @Param("status") String status,
            @Param("reason") String reason,
            @Param("postId") Long postId,
            @Param("keyword") String keyword,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Pageable pageable);

    @Query(value = """
            SELECT r.moderation_case_id AS caseId, r.reason AS reason, COUNT(*) AS reasonCount
            FROM reports r
            WHERE r.moderation_case_id IN (:caseIds)
            GROUP BY r.moderation_case_id, r.reason
            ORDER BY r.moderation_case_id, COUNT(*) DESC, r.reason
            """, nativeQuery = true)
    List<ModerationReasonCountProjection> findReasonCounts(@Param("caseIds") Collection<Long> caseIds);

    @Query(value = """
            SELECT mc.id AS caseId, mc.status AS status, mc.report_count AS reportCount,
                   (SELECT COUNT(DISTINCT r.reporter_id) FROM reports r
                    WHERE r.moderation_case_id = mc.id) AS distinctReporterCount,
                   mc.first_reported_at AS firstReportedAt,
                   mc.latest_reported_at AS latestReportedAt,
                   p.id AS postId, p.status AS postCurrentStatus, p.content AS postCurrentContent,
                   p.author_id AS authorId, ap.display_name AS authorDisplayName,
                   ap.avatar_url AS authorAvatarUrl, au.status AS authorAccountStatus,
                   p.hidden_at AS hiddenAt, p.hidden_reason AS hiddenReason, p.deleted_at AS deletedAt,
                   mc.resolved_by AS resolvedByAdminId, rp.display_name AS resolvedByDisplayName,
                   mc.resolved_at AS resolvedAt, mc.resolution_note AS resolutionNote
            FROM moderation_cases mc
            JOIN posts p ON p.id = mc.post_id
            JOIN users au ON au.id = p.author_id
            LEFT JOIN user_profiles ap ON ap.user_id = p.author_id
            LEFT JOIN user_profiles rp ON rp.user_id = mc.resolved_by
            WHERE mc.id = :caseId
            """, nativeQuery = true)
    Optional<AdminModerationCaseDetailProjection> findCaseDetail(@Param("caseId") Long caseId);

    @Query(value = """
            SELECT r.id AS reportId, r.status AS status, r.reason AS reason,
                   r.description AS description, r.reporter_id AS reporterId,
                   up.display_name AS reporterDisplayName, up.avatar_url AS reporterAvatarUrl,
                   u.status AS reporterAccountStatus,
                   r.post_content_snapshot AS contentSnapshot,
                   CAST(r.post_media_snapshot AS CHAR) AS mediaSnapshot,
                   r.created_at AS createdAt, r.resolved_at AS resolvedAt
            FROM reports r
            JOIN users u ON u.id = r.reporter_id
            LEFT JOIN user_profiles up ON up.user_id = r.reporter_id
            WHERE r.moderation_case_id = :caseId
            ORDER BY r.created_at DESC, r.id DESC
            """, nativeQuery = true)
    List<AdminModerationCaseReportProjection> findCaseReports(@Param("caseId") Long caseId);

    @Query(value = """
            SELECT aa.id AS actionId, aa.action_type AS actionType,
                   aa.admin_id AS adminId, up.display_name AS adminDisplayName,
                   aa.note AS note, aa.created_at AS createdAt
            FROM admin_actions aa
            LEFT JOIN user_profiles up ON up.user_id = aa.admin_id
            WHERE aa.target_type = 'MODERATION_CASE' AND aa.target_id = :caseId
            ORDER BY aa.created_at DESC, aa.id DESC
            """, nativeQuery = true)
    List<AdminModerationCaseActionProjection> findCaseActions(@Param("caseId") Long caseId);
}
