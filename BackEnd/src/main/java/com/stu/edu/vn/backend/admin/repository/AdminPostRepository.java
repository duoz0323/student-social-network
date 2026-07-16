package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.admin.repository.projection.AdminPostDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostHashtagProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminPostMediaProjection;
import com.stu.edu.vn.backend.post.entity.Post;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Repository đọc projection và khóa Post tối thiểu cho nghiệp vụ quản trị. */
public interface AdminPostRepository extends Repository<Post, Long> {

    /** Khóa đúng hàng posts mục tiêu đến hết transaction để tuần tự hóa các lần đổi trạng thái đồng thời. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findByIdForUpdate(@Param("postId") Long postId);

    /** Chỉ đọc tên hiển thị của ADMIN để tạo response, không tải toàn bộ UserProfile. */
    @Query(value = "SELECT up.display_name FROM user_profiles up WHERE up.user_id = :adminId", nativeQuery = true)
    Optional<String> findAdminDisplayName(@Param("adminId") Long adminId);

    @Query(value = """
            SELECT p.id AS postId, p.content AS contentPreview, p.status AS status,
                   a.id AS authorId, ap.display_name AS authorDisplayName,
                   ap.avatar_url AS authorAvatarUrl, a.status AS authorAccountStatus,
                   (SELECT pm.media_url FROM post_media pm WHERE pm.post_id = p.id
                    ORDER BY pm.display_order ASC, pm.id ASC LIMIT 1) AS thumbnailUrl,
                   (SELECT COUNT(*) FROM post_media pmc WHERE pmc.post_id = p.id) AS mediaCount,
                   p.like_count AS likeCount, p.comment_count AS commentCount,
                   (SELECT COUNT(*) FROM reports pr
                    WHERE pr.post_id = p.id AND pr.status = 'PENDING') AS pendingReportCount,
                   p.created_at AS createdAt, p.updated_at AS updatedAt
            FROM posts p
            JOIN users a ON a.id = p.author_id
            LEFT JOIN user_profiles ap ON ap.user_id = a.id
            WHERE (:status IS NULL OR p.status = :status)
              AND (:authorId IS NULL OR p.author_id = :authorId)
              AND (:keyword IS NULL
                   OR LOWER(p.content) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(ap.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
              AND (:reportedOnly = 0 OR EXISTS (
                   SELECT 1 FROM reports er WHERE er.post_id = p.id AND er.status = 'PENDING'))
            ORDER BY p.created_at DESC, p.id DESC
            """,
            countQuery = """
            SELECT COUNT(p.id)
            FROM posts p
            JOIN users a ON a.id = p.author_id
            LEFT JOIN user_profiles ap ON ap.user_id = a.id
            WHERE (:status IS NULL OR p.status = :status)
              AND (:authorId IS NULL OR p.author_id = :authorId)
              AND (:keyword IS NULL
                   OR LOWER(p.content) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(ap.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
              AND (:reportedOnly = 0 OR EXISTS (
                   SELECT 1 FROM reports er WHERE er.post_id = p.id AND er.status = 'PENDING'))
            """, nativeQuery = true)
    Page<AdminPostListProjection> findAdminPosts(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("authorId") Long authorId,
            @Param("reportedOnly") int reportedOnly,
            Pageable pageable);

    @Query(value = """
            SELECT p.id AS postId, p.content AS content, p.status AS status,
                   a.id AS authorId, ap.display_name AS authorDisplayName,
                   ap.avatar_url AS authorAvatarUrl, a.email AS authorEmail,
                   a.phone_number AS authorPhoneNumber, a.status AS authorAccountStatus,
                   p.like_count AS likeCount, p.comment_count AS commentCount,
                   (SELECT COUNT(*) FROM reports pr
                    WHERE pr.post_id = p.id AND pr.status = 'PENDING') AS pendingReportCount,
                   (SELECT COUNT(*) FROM reports tr WHERE tr.post_id = p.id) AS totalReportCount,
                   p.hidden_at AS hiddenAt, p.hidden_reason AS hiddenReason,
                   p.hidden_by AS hiddenByAdminId, hp.display_name AS hiddenByDisplayName,
                   p.deleted_at AS deletedAt, p.created_at AS createdAt, p.updated_at AS updatedAt
            FROM posts p
            JOIN users a ON a.id = p.author_id
            LEFT JOIN user_profiles ap ON ap.user_id = a.id
            LEFT JOIN users ha ON ha.id = p.hidden_by
            LEFT JOIN user_profiles hp ON hp.user_id = ha.id
            WHERE p.id = :postId
            """, nativeQuery = true)
    Optional<AdminPostDetailProjection> findAdminPostDetail(@Param("postId") Long postId);

    @Query(value = """
            SELECT pm.id AS mediaId, pm.media_url AS mediaUrl, pm.display_order AS sortOrder
            FROM post_media pm
            WHERE pm.post_id = :postId
            ORDER BY pm.display_order ASC, pm.id ASC
            """, nativeQuery = true)
    List<AdminPostMediaProjection> findAdminPostMedia(@Param("postId") Long postId);

    @Query(value = """
            SELECT DISTINCT h.normalized_name AS name
            FROM post_hashtags ph
            JOIN hashtags h ON h.id = ph.hashtag_id
            WHERE ph.post_id = :postId
            ORDER BY h.normalized_name ASC
            """, nativeQuery = true)
    List<AdminPostHashtagProjection> findAdminPostHashtags(@Param("postId") Long postId);
}
