package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.admin.repository.projection.AdminReportDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminReportListProjection;
import com.stu.edu.vn.backend.report.entity.Report;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Repository projection chỉ đọc cho hai màn hình quản trị báo cáo. */
public interface AdminReportRepository extends Repository<Report, Long> {

    /** Khóa đúng report mục tiêu để chỉ một ADMIN được chuyển trạng thái khỏi PENDING. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.id = :reportId and r.moderationCase is null")
    Optional<Report> findByIdForUpdate(@Param("reportId") Long reportId);

    @Query(value = """
            SELECT r.id AS reportId, r.status AS status, r.reason AS reason,
                   r.description AS description,
                   reporter.id AS reporterId, rp.display_name AS reporterDisplayName,
                   rp.avatar_url AS reporterAvatarUrl, reporter.status AS reporterAccountStatus,
                   p.id AS postId, p.status AS postCurrentStatus,
                   r.post_content_snapshot AS contentPreview,
                   author.id AS authorId, ap.display_name AS authorDisplayName,
                   author.status AS authorAccountStatus,
                   COALESCE(JSON_LENGTH(r.post_media_snapshot), 0) AS snapshotMediaCount,
                   r.created_at AS createdAt
            FROM reports r
            JOIN users reporter ON reporter.id = r.reporter_id
            LEFT JOIN user_profiles rp ON rp.user_id = reporter.id
            JOIN posts p ON p.id = r.post_id
            JOIN users author ON author.id = p.author_id
            LEFT JOIN user_profiles ap ON ap.user_id = author.id
            WHERE (:status IS NULL OR r.status = :status)
              AND r.moderation_case_id IS NULL
              AND (:reason IS NULL OR r.reason = :reason)
              AND (:postId IS NULL OR r.post_id = :postId)
              AND (:reporterId IS NULL OR r.reporter_id = :reporterId)
              AND (:authorId IS NULL OR p.author_id = :authorId)
              AND (:keyword IS NULL
                   OR LOWER(r.post_content_snapshot) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(rp.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(ap.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
            ORDER BY
              CASE WHEN :pendingOrder = 1 THEN r.created_at END ASC,
              CASE WHEN :pendingOrder = 1 THEN r.id END ASC,
              CASE WHEN :pendingOrder = 0 THEN r.created_at END DESC,
              CASE WHEN :pendingOrder = 0 THEN r.id END DESC
            """, countQuery = """
            SELECT COUNT(r.id)
            FROM reports r
            JOIN users reporter ON reporter.id = r.reporter_id
            LEFT JOIN user_profiles rp ON rp.user_id = reporter.id
            JOIN posts p ON p.id = r.post_id
            JOIN users author ON author.id = p.author_id
            LEFT JOIN user_profiles ap ON ap.user_id = author.id
            WHERE (:status IS NULL OR r.status = :status)
              AND r.moderation_case_id IS NULL
              AND (:reason IS NULL OR r.reason = :reason)
              AND (:postId IS NULL OR r.post_id = :postId)
              AND (:reporterId IS NULL OR r.reporter_id = :reporterId)
              AND (:authorId IS NULL OR p.author_id = :authorId)
              AND (:keyword IS NULL
                   OR LOWER(r.post_content_snapshot) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(rp.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(ap.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
            """, nativeQuery = true)
    Page<AdminReportListProjection> findAdminReports(
            @Param("status") String status,
            @Param("reason") String reason,
            @Param("postId") Long postId,
            @Param("reporterId") Long reporterId,
            @Param("authorId") Long authorId,
            @Param("keyword") String keyword,
            @Param("pendingOrder") int pendingOrder,
            Pageable pageable);

    @Query(value = """
            SELECT r.id AS reportId, r.status AS status, r.reason AS reason,
                   r.description AS description,
                   reporter.id AS reporterId, rp.display_name AS reporterDisplayName,
                   rp.avatar_url AS reporterAvatarUrl, reporter.status AS reporterAccountStatus,
                   p.id AS postId, p.status AS postCurrentStatus, p.content AS postCurrentContent,
                   author.id AS authorId, ap.display_name AS authorDisplayName,
                   ap.avatar_url AS authorAvatarUrl, author.status AS authorAccountStatus,
                   p.hidden_at AS hiddenAt, p.hidden_reason AS hiddenReason, p.deleted_at AS deletedAt,
                   r.post_content_snapshot AS contentSnapshot,
                   CAST(r.post_media_snapshot AS CHAR) AS mediaSnapshot,
                   resolver.id AS resolvedByAdminId, rsp.display_name AS resolvedByDisplayName,
                   r.resolved_at AS resolvedAt, r.resolution_note AS resolutionNote,
                   r.created_at AS createdAt
            FROM reports r
            JOIN users reporter ON reporter.id = r.reporter_id
            LEFT JOIN user_profiles rp ON rp.user_id = reporter.id
            JOIN posts p ON p.id = r.post_id
            JOIN users author ON author.id = p.author_id
            LEFT JOIN user_profiles ap ON ap.user_id = author.id
            LEFT JOIN users resolver ON resolver.id = r.resolved_by
            LEFT JOIN user_profiles rsp ON rsp.user_id = resolver.id
            WHERE r.id = :reportId AND r.moderation_case_id IS NULL
            """, nativeQuery = true)
    Optional<AdminReportDetailProjection> findAdminReportDetail(@Param("reportId") Long reportId);
}
