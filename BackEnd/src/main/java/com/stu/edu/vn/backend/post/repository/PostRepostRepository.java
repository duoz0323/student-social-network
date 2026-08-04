package com.stu.edu.vn.backend.post.repository;

import com.stu.edu.vn.backend.post.entity.PostRepost;
import com.stu.edu.vn.backend.post.entity.PostRepostId;
import com.stu.edu.vn.backend.post.repository.projection.FeedActivityProjection;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập quan hệ Repost và các timeline keyset mà không chứa logic nghiệp vụ. */
public interface PostRepostRepository extends JpaRepository<PostRepost, PostRepostId> {

    boolean existsByIdUserIdAndIdPostId(Long userId, Long postId);

    @Modifying
    @Query(value = """
            INSERT INTO post_reposts(user_id, post_id)
            VALUES (:userId, :postId)
            ON DUPLICATE KEY UPDATE user_id = post_reposts.user_id
            """, nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("select repost.id.postId from PostRepost repost "
            + "where repost.id.userId = :userId and repost.id.postId in :postIds")
    List<Long> findRepostedPostIds(
            @Param("userId") Long userId,
            @Param("postIds") Collection<Long> postIds
    );

    @Modifying
    @Query(value = "DELETE FROM post_reposts WHERE user_id = :userId AND post_id = :postId", nativeQuery = true)
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * Timeline Profile chỉ đọc Repost có bài gốc và hai tài khoản còn hợp lệ; khóa đầy đủ là
     * created_at, itemRank, actorId, postId để dùng chung codec với Following Feed.
     */
    @Query(value = """
            SELECT pr.post_id AS postId,
                   pr.created_at AS activityAt,
                   1 AS itemRank,
                   pr.user_id AS actorId,
                   pr.created_at AS repostedAt
            FROM post_reposts pr
            JOIN posts p ON p.id = pr.post_id
            JOIN users original_user ON original_user.id = p.author_id
            JOIN user_profiles original_profile ON original_profile.user_id = p.author_id
            JOIN users reposter ON reposter.id = pr.user_id
            JOIN user_profiles reposter_profile ON reposter_profile.user_id = pr.user_id
            WHERE pr.user_id = :profileUserId
              AND p.status = 'PUBLISHED'
              AND original_user.status = 'ACTIVE'
              AND original_profile.profile_completed_at IS NOT NULL
              AND reposter.status = 'ACTIVE'
              AND reposter_profile.profile_completed_at IS NOT NULL
              AND (
                    pr.created_at < :cursorTime
                    OR (pr.created_at = :cursorTime AND pr.post_id < :cursorPostId)
              )
            ORDER BY pr.created_at DESC, pr.post_id DESC
            """, nativeQuery = true)
    List<FeedActivityProjection> findProfileRepostActivities(
            @Param("profileUserId") Long profileUserId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );

    /**
     * Hợp nhất bài gốc và Repost của người được Follow bằng UNION ALL; điều kiện keyset lặp ở
     * từng nhánh để database chỉ đọc phần timeline nằm sau cursor.
     */
    @Query(value = """
            SELECT activity.postId AS postId,
                   activity.activityAt AS activityAt,
                   activity.itemRank AS itemRank,
                   activity.actorId AS actorId,
                   activity.repostedAt AS repostedAt
            FROM (
                SELECT p.id AS postId,
                       p.published_at AS activityAt,
                       0 AS itemRank,
                       p.author_id AS actorId,
                       NULL AS repostedAt
                FROM posts p
                JOIN follows f ON f.following_id = p.author_id
                JOIN users original_user ON original_user.id = p.author_id
                JOIN user_profiles original_profile ON original_profile.user_id = p.author_id
                WHERE f.follower_id = :viewerId
                  AND p.status = 'PUBLISHED'
                  AND original_user.status = 'ACTIVE'
                  AND original_profile.profile_completed_at IS NOT NULL

                UNION ALL

                SELECT p.id AS postId,
                       pr.created_at AS activityAt,
                       1 AS itemRank,
                       pr.user_id AS actorId,
                       pr.created_at AS repostedAt
                FROM post_reposts pr
                JOIN follows f ON f.following_id = pr.user_id
                JOIN users reposter ON reposter.id = pr.user_id
                JOIN user_profiles reposter_profile ON reposter_profile.user_id = pr.user_id
                JOIN posts p ON p.id = pr.post_id
                JOIN users original_user ON original_user.id = p.author_id
                JOIN user_profiles original_profile ON original_profile.user_id = p.author_id
                WHERE f.follower_id = :viewerId
                  AND p.status = 'PUBLISHED'
                  AND reposter.status = 'ACTIVE'
                  AND reposter_profile.profile_completed_at IS NOT NULL
                  AND original_user.status = 'ACTIVE'
                  AND original_profile.profile_completed_at IS NOT NULL
            ) activity
            WHERE activity.activityAt < :cursorTime
               OR (activity.activityAt = :cursorTime AND activity.itemRank < :cursorItemRank)
               OR (activity.activityAt = :cursorTime AND activity.itemRank = :cursorItemRank
                   AND activity.actorId < :cursorActorId)
               OR (activity.activityAt = :cursorTime AND activity.itemRank = :cursorItemRank
                   AND activity.actorId = :cursorActorId AND activity.postId < :cursorPostId)
            ORDER BY activity.activityAt DESC,
                     activity.itemRank DESC,
                     activity.actorId DESC,
                     activity.postId DESC
            """, nativeQuery = true)
    List<FeedActivityProjection> findFollowingActivities(
            @Param("viewerId") Long viewerId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorItemRank") int cursorItemRank,
            @Param("cursorActorId") Long cursorActorId,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );
}
