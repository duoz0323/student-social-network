package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.projection.PostInteractionTargetProjection;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Collection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy vấn bài viết theo id, trạng thái và tác giả mà không chứa nghiệp vụ.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /** Khóa bài gốc để tuần tự hóa Repost concurrent và thay đổi trạng thái của cùng bài. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"author", "authorProfile"})
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findRepostTargetByIdForUpdate(@Param("postId") Long postId);

    /** Khóa đúng Post để tuần tự hóa thao tác tìm hoặc tạo Moderation Case OPEN. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p join fetch p.author where p.id = :postId")
    Optional<Post> findReportTargetByIdForUpdate(@Param("postId") Long postId);

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
                      AND NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :viewerId AND ub.blocked_id = p.author_id)
                             OR (ub.blocker_id = p.author_id AND ub.blocked_id = :viewerId)
                      )
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
                      AND NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :viewerId AND ub.blocked_id = p.author_id)
                             OR (ub.blocker_id = p.author_id AND ub.blocked_id = :viewerId)
                      )
                      AND (p.published_at < :cursorTime OR (p.published_at = :cursorTime AND p.id < :cursorPostId))
                    ORDER BY p.published_at DESC, p.id DESC
                    """, nativeQuery = true)
    List<Post> findProfilePosts(
            @Param("authorId") Long authorId,
            @Param("viewerId") Long viewerId,
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
                      AND NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :viewerId AND ub.blocked_id = p.author_id)
                             OR (ub.blocker_id = p.author_id AND ub.blocked_id = :viewerId)
                      )
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
                      AND NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :viewerId AND ub.blocked_id = p.author_id)
                             OR (ub.blocker_id = p.author_id AND ub.blocked_id = :viewerId)
                      )
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
    @Query(value = """
                    SELECT p.*
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    WHERE p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND p.content IS NOT NULL
                      AND NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :viewerId AND ub.blocked_id = p.author_id)
                             OR (ub.blocker_id = p.author_id AND ub.blocked_id = :viewerId)
                      )
                      AND MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                      AND (
                          :cursorRelevance IS NULL
                          OR MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE) < :cursorRelevance
                          OR (MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE) = :cursorRelevance
                              AND p.published_at < :cursorTime)
                          OR (MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE) = :cursorRelevance
                              AND p.published_at = :cursorTime AND p.id < :cursorPostId)
                      )
                    ORDER BY MATCH(p.content) AGAINST (:keyword IN NATURAL LANGUAGE MODE) DESC,
                             p.published_at DESC,
                             p.id DESC
                    """, nativeQuery = true)
    List<Post> searchPublishedPostsByContentAfter(
            @Param("keyword") String keyword,
            @Param("viewerId") Long viewerId,
            @Param("cursorRelevance") Double cursorRelevance,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    /** Đọc đúng điểm FULLTEXT của bài cuối trang để tạo cursor opaque cho trang kế tiếp. */
    @Query(value = "SELECT MATCH(content) AGAINST (:keyword IN NATURAL LANGUAGE MODE) FROM posts WHERE id = :postId",
            nativeQuery = true)
    Optional<Double> findContentSearchRelevance(
            @Param("postId") Long postId,
            @Param("keyword") String keyword
    );

    /**
     * Tìm bài theo exact normalized hashtag, không join collection hiển thị để pagination và totalElements luôn chính xác.
     */
    @Query(value = """
                    SELECT p.*
                    FROM posts p
                    JOIN users u ON u.id = p.author_id
                    JOIN user_profiles up ON up.user_id = p.author_id
                    JOIN post_hashtags ph ON ph.post_id = p.id
                    JOIN hashtags h ON h.id = ph.hashtag_id
                    WHERE p.status = 'PUBLISHED'
                      AND u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND NOT EXISTS (
                          SELECT 1 FROM user_blocks ub
                          WHERE (ub.blocker_id = :viewerId AND ub.blocked_id = p.author_id)
                             OR (ub.blocker_id = p.author_id AND ub.blocked_id = :viewerId)
                      )
                      AND h.normalized_name = :normalizedName
                      AND (p.published_at < :cursorTime
                           OR (p.published_at = :cursorTime AND p.id < :cursorPostId))
                    ORDER BY p.published_at DESC, p.id DESC
                    """, nativeQuery = true)
    List<Post> searchPublishedPostsByHashtagAfter(
            @Param("normalizedName") String normalizedName,
            @Param("viewerId") Long viewerId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    // Tìm bài theo id và trạng thái để loại bài HIDDEN/DELETED khỏi truy vấn thông thường.
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);

    /** Batch-load đúng các PostCard mà viewer còn quyền xem để Messaging không làm lộ preview cũ. */
    @Query("""
            SELECT post
            FROM Post post
            JOIN FETCH post.author author
            WHERE post.id IN :postIds
              AND post.status = :status
              AND author.role = com.stu.edu.vn.backend.user.enums.UserRole.USER
              AND author.status = com.stu.edu.vn.backend.user.enums.UserStatus.ACTIVE
              AND EXISTS (
                  SELECT profile.userId FROM UserProfile profile
                  WHERE profile.userId = author.id AND profile.profileCompletedAt IS NOT NULL
              )
              AND NOT EXISTS (
                  SELECT blockRelation.id.blockerId FROM UserBlock blockRelation
                  WHERE (blockRelation.id.blockerId = :viewerId AND blockRelation.id.blockedId = author.id)
                     OR (blockRelation.id.blockerId = author.id AND blockRelation.id.blockedId = :viewerId)
              )
            """)
    List<Post> findVisibleSharedPosts(@Param("viewerId") Long viewerId,
                                      @Param("postIds") Collection<Long> postIds,
                                      @Param("status") PostStatus status);

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

    // Đọc counter mới nhất sau khi trigger post_reposts đã chạy trong transaction hiện tại.
    @Query(value = "SELECT repost_count FROM posts WHERE id = :postId", nativeQuery = true)
    Optional<Integer> findRepostCountById(@Param("postId") Long postId);

    // Batch-load header bài gốc cho timeline hoạt động; thứ tự được khôi phục từ projection bên Service.
    @EntityGraph(attributePaths = {"author", "location"})
    @Query("""
            select distinct p
            from Post p
            where p.id in :postIds
              and p.status = com.stu.edu.vn.backend.post.enums.PostStatus.PUBLISHED
            """)
    List<Post> findFeedHeadersByIds(@Param("postIds") Collection<Long> postIds);

    // Batch-load láº¡i PostCard sau ranking vÃ  tÃ¡i kiá»ƒm tra access Ä‘á»ƒ race Block/status khÃ´ng lÃ m lá»™ bÃ i.
    @EntityGraph(attributePaths = {"author", "location"})
    @Query("""
            select distinct p
            from Post p
            join p.author author
            join p.authorProfile authorProfile
            where p.id in :postIds
              and p.status = com.stu.edu.vn.backend.post.enums.PostStatus.PUBLISHED
              and author.status = com.stu.edu.vn.backend.user.enums.UserStatus.ACTIVE
              and authorProfile.profileCompletedAt is not null
              and not exists (
                  select blockRelation.id
                  from UserBlock blockRelation
                  where (blockRelation.id.blockerId = :viewerId and blockRelation.id.blockedId = p.author.id)
                     or (blockRelation.id.blockerId = p.author.id and blockRelation.id.blockedId = :viewerId)
              )
            """)
    List<Post> findAccessibleFeedHeadersByIds(
            @Param("viewerId") Long viewerId,
            @Param("postIds") Collection<Long> postIds
    );

    // Kiểm tra quyền sở hữu bài viết trước khi cho phép tác giả sửa hoặc xóa mềm.
    boolean existsByIdAndAuthor_Id(Long id, Long authorId);

    // Fetch tác giả và hồ sơ tác giả cho API chi tiết, còn media/hashtag tải bằng repository riêng.
    @EntityGraph(attributePaths = {"author", "authorProfile", "location"})
    @Query("select p from Post p where p.id = :postId and p.status = :status")
    Optional<Post> findDetailHeaderByIdAndStatus(
            @Param("postId") Long postId,
            @Param("status") PostStatus status
    );

    /** Khóa Post trong transaction cập nhật ngắn sau khi moderation và upload đã hoàn tất. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"author", "authorProfile", "location"})
    @Query("select p from Post p where p.id = :postId and p.status = :status")
    Optional<Post> findDetailHeaderByIdAndStatusForUpdate(
            @Param("postId") Long postId,
            @Param("status") PostStatus status
    );

    // Màn hình quản lý của tác giả được xem lại cả bài HIDDEN/DELETED nhưng không được xem bài của người khác.
    @EntityGraph(attributePaths = {"author", "authorProfile", "location"})
    @Query("select p from Post p where p.id = :postId and p.author.id = :authorId")
    Optional<Post> findOwnedDetailHeaderById(
            @Param("postId") Long postId,
            @Param("authorId") Long authorId
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
