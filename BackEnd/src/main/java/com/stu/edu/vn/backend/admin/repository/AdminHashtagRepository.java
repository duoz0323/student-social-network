package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.admin.repository.projection.AdminHashtagListProjection;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository chỉ đọc dữ liệu tổng hợp hashtag cho khu vực quản trị. */
public interface AdminHashtagRepository extends JpaRepository<Hashtag, Long> {

    boolean existsByNormalizedName(String normalizedName);

    boolean existsByNormalizedNameAndIdNot(String normalizedName, Long hashtagId);

    /** Khóa hashtag để thao tác xóa không chạy đồng thời với một lần xóa khác. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hashtag h where h.id = :hashtagId")
    Optional<Hashtag> findByIdForUpdate(@Param("hashtagId") Long hashtagId);

    /** Gỡ hashtag khỏi mọi bài trước khi xóa bản ghi vì khóa ngoại hiện dùng ON DELETE RESTRICT. */
    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM post_hashtags WHERE hashtag_id = :hashtagId", nativeQuery = true)
    int deletePostRelations(@Param("hashtagId") Long hashtagId);

    @Query(value = """
            SELECT h.id AS hashtagId, h.display_name AS name, h.post_count AS postCount,
                   h.created_at AS createdAt, MAX(ph.created_at) AS latestUsedAt
            FROM hashtags h
            LEFT JOIN post_hashtags ph ON ph.hashtag_id = h.id
            WHERE (:keyword IS NULL
                   OR LOWER(h.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(h.normalized_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
            GROUP BY h.id, h.display_name, h.post_count, h.created_at
            ORDER BY (MAX(ph.created_at) IS NULL) ASC, MAX(ph.created_at) DESC, h.id DESC
            """,
            countQuery = """
            SELECT COUNT(h.id)
            FROM hashtags h
            WHERE (:keyword IS NULL
                   OR LOWER(h.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                   OR LOWER(h.normalized_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '=')
            """, nativeQuery = true)
    Page<AdminHashtagListProjection> findAdminHashtags(
            @Param("keyword") String keyword,
            Pageable pageable);
}
