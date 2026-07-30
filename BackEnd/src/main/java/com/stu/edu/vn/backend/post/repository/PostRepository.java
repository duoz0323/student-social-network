package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.projection.PostInteractionTargetProjection;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy vấn bài viết theo id, trạng thái và tác giả mà không chứa nghiệp vụ.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
                    SELECT p.*
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND (
                          (p.like_count + p.comment_count) < :cursorScore
                          OR ((p.like_count + p.comment_count) = :cursorScore AND p.published_at < :cursorTime)
                          OR ((p.like_count + p.comment_count) = :cursorScore AND p.published_at = :cursorTime AND p.id < :cursorPostId)
                      )
                    ORDER BY (p.like_count + p.comment_count) DESC, p.published_at DESC, p.id DESC
                    """, nativeQuery = true)
    List<Post> findForYouFeed(
            @Param("cursorScore") int cursorScore,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    @Query(value = """
                    SELECT p.*
                    FROM posts p
                    JOIN follows f ON f.following_id = p.author_id
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE f.follower_id = :viewerId
                      AND p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND (p.published_at < :cursorTime OR (p.published_at = :cursorTime AND p.id < :cursorPostId))
                    ORDER BY p.published_at DESC, p.id DESC
                    """, nativeQuery = true)
    List<Post> findFollowingFeed(
            @Param("viewerId") Long viewerId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    @Query(value = """
                    SELECT p.*
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE p.author_id = :authorId
                      AND p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND (p.published_at < :cursorTime OR (p.published_at = :cursorTime AND p.id < :cursorPostId))
                    ORDER BY p.published_at DESC, p.id DESC
                    """, nativeQuery = true)
    List<Post> findProfilePosts(
            @Param("authorId") Long authorId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    @Query(value = """
                    SELECT p.*
                    FROM saved_posts sp
                    JOIN posts p ON p.id = sp.post_id
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE sp.user_id = :viewerId
                      AND p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND (sp.created_at < :cursorTime OR (sp.created_at = :cursorTime AND p.id < :cursorPostId))
                    ORDER BY sp.created_at DESC, p.id DESC
                    """, nativeQuery = true)
    // Danh sách chỉ chứa bài còn khả dụng và giữ đúng thứ tự người dùng lưu gần nhất.
    List<Post> findSavedPosts(
            @Param("viewerId") Long viewerId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    @Query(value = """
                    SELECT p.*
                    FROM post_likes pl
                    JOIN posts p ON p.id = pl.post_id
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE pl.user_id = :viewerId
                      AND p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND (pl.created_at < :cursorTime OR (pl.created_at = :cursorTime AND p.id < :cursorPostId))
                    ORDER BY pl.created_at DESC, p.id DESC
                    """, nativeQuery = true)
    // Danh sách chỉ chứa bài còn khả dụng và giữ đúng thứ tự người dùng Like gần nhất.
    List<Post> findLikedPosts(
            @Param("viewerId") Long viewerId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    /**
     * Tìm nội dung bằng FULLTEXT MySQL nhưng chỉ trả bài công khai của tác giả đang hoạt động và đã hoàn tất hồ sơ.
     */
    @Query(
            value = """
                    SELECT p.*
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND p.content IS NOT NULL
                      AND MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                    ORDER BY MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE) DESC,
                             p.published_at DESC,
                             p.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND p.content IS NOT NULL
                      AND MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                    """,
            nativeQuery = true
    )
    Page<Post> searchPublishedPostsByContent(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm bài theo exact normalized hashtag, không join collection hiển thị để pagination và totalElements luôn chính xác.
     */
    @Query(
            value = """
                    SELECT p.*
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    JOIN post_hashtags ph ON ph.post_id = p.id
                    JOIN hashtags h ON h.id = ph.hashtag_id
                    WHERE p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND h.normalized_name = :normalizedName
                    ORDER BY p.published_at DESC, p.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    JOIN post_hashtags ph ON ph.post_id = p.id
                    JOIN hashtags h ON h.id = ph.hashtag_id
                    WHERE p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND h.normalized_name = :normalizedName
                    """,
            nativeQuery = true
    )
    Page<Post> searchPublishedPostsByHashtag(@Param("normalizedName") String normalizedName, Pageable pageable);

    // Tìm bài theo id và trạng thái để loại bài HIDDEN/DELETED khỏi truy vấn thông thường.
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);

    // Lấy riêng trạng thái để Like/Unlike phân biệt post không tồn tại với post HIDDEN/DELETED.
    @Query("select p.status from Post p where p.id = :postId")
    Optional<PostStatus> findStatusById(@Param("postId") Long postId);

    // Lấy trạng thái và tác giả trong một query để tạo Notification không phát sinh truy vấn lazy ngoài ý muốn.
    @Query("""
            SELECT post.id AS postId,
                   post.author.id AS authorId,
                   post.status AS status
            FROM Post post
            WHERE post.id = :postId
            """)
    Optional<PostInteractionTargetProjection> findInteractionTargetById(@Param("postId") Long postId);

    // Tải một lần bài viết, tác giả và media để Report tạo snapshot nhất quán, tránh N+1.
    @EntityGraph(attributePaths = {"author", "media"})
    @Query("select distinct p from Post p where p.id = :postId")
    Optional<Post> findReportSnapshotById(@Param("postId") Long postId);

    // Đọc like_count mới nhất từ database sau khi trigger post_likes chạy, Service không tự cộng/trừ bộ đếm.
    @Query(
            value = "SELECT like_count FROM posts WHERE id = :postId",
            nativeQuery = true
    )
    Optional<Integer> findLikeCountById(@Param("postId") Long postId);

    // Kiểm tra quyền sở hữu bài viết trước khi cho phép tác giả sửa hoặc xóa mềm.
    boolean existsByIdAndAuthor_Id(Long id, Long authorId);

    // Fetch tác giả và hồ sơ tác giả cho API chi tiết, còn media/hashtag tải bằng repository riêng.
    @EntityGraph(attributePaths = {"author", "authorProfile", "location"})
    @Query("select p from Post p where p.id = :postId and p.status = :status")
    Optional<Post> findDetailHeaderByIdAndStatus(
            @Param("postId") Long postId,
            @Param("status") PostStatus status
    );

    // Cập nhật cờ đã chỉnh sửa và updated_at ngay cả khi người dùng chỉ đổi hashtag hoặc media.
    @Modifying
    @Query(
            value = "UPDATE posts SET is_edited = TRUE, updated_at = CURRENT_TIMESTAMP(6) WHERE id = :postId",
            nativeQuery = true
    )
    void markEdited(@Param("postId") Long postId);

    // Xóa mềm chỉ đổi trạng thái bài PUBLISHED, không xóa cứng posts hoặc dữ liệu liên quan.
    @Modifying
    @Query(
            value = """
                    UPDATE posts
                    SET status = 'DELETED',
                        deleted_at = :deletedAt,
                        updated_at = CURRENT_TIMESTAMP(6)
                    WHERE id = :postId
                      AND status = 'PUBLISHED'
                    """,
            nativeQuery = true
    )
    int softDeletePublishedPost(
            @Param("postId") Long postId,
            @Param("deletedAt") LocalDateTime deletedAt
    );
}
