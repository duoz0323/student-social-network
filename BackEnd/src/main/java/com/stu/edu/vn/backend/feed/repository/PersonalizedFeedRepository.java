package com.stu.edu.vn.backend.feed.repository;

import com.stu.edu.vn.backend.feed.repository.projection.PersonalizedPostRankProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Truy vấn ranking deterministic của Feed For You trực tiếp tại MySQL 8. */
public interface PersonalizedFeedRepository extends Repository<com.stu.edu.vn.backend.post.entity.Post, Long> {

    @Query(value = """
            WITH viewer_profile AS (
                SELECT school_id, faculty_id, major_id, entry_year
                FROM user_profiles
                WHERE user_id = :viewerId
            ),
            candidate_posts AS (
                SELECT p.id AS post_id, p.author_id, p.published_at,
                       p.like_count, p.comment_count, p.repost_count
                FROM posts p
                JOIN users author ON author.id = p.author_id
                JOIN user_profiles author_profile ON author_profile.user_id = p.author_id
                WHERE p.status = 'PUBLISHED'
                  AND author.status = 'ACTIVE'
                  AND author_profile.profile_completed_at IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1
                      FROM user_blocks blocked_relation
                      WHERE (blocked_relation.blocker_id = :viewerId AND blocked_relation.blocked_id = p.author_id)
                         OR (blocked_relation.blocker_id = p.author_id AND blocked_relation.blocked_id = :viewerId)
                  )
            ),
            common_interests AS (
                SELECT author_interest.user_id AS author_id,
                       LEAST(COUNT(*), 5) * 2 AS interest_score
                FROM user_interests viewer_interest
                JOIN interest_categories interest
                  ON interest.id = viewer_interest.interest_id AND interest.status = 'ACTIVE'
                JOIN user_interests author_interest
                  ON author_interest.interest_id = viewer_interest.interest_id
                WHERE viewer_interest.user_id = :viewerId
                GROUP BY author_interest.user_id
            ),
            liked_authors AS (
                SELECT historical_post.author_id, COUNT(*) AS interaction_count
                FROM post_likes historical_like
                JOIN posts historical_post ON historical_post.id = historical_like.post_id
                WHERE historical_like.user_id = :viewerId
                GROUP BY historical_post.author_id
            ),
            commented_authors AS (
                SELECT historical_post.author_id, COUNT(*) AS interaction_count
                FROM comments historical_comment
                JOIN posts historical_post ON historical_post.id = historical_comment.post_id
                WHERE historical_comment.user_id = :viewerId
                  AND historical_comment.status = 'PUBLISHED'
                GROUP BY historical_post.author_id
            ),
            saved_authors AS (
                SELECT historical_post.author_id, COUNT(*) AS interaction_count
                FROM saved_posts historical_save
                JOIN posts historical_post ON historical_post.id = historical_save.post_id
                WHERE historical_save.user_id = :viewerId
                GROUP BY historical_post.author_id
            ),
            reposted_authors AS (
                SELECT historical_post.author_id, COUNT(*) AS interaction_count
                FROM post_reposts historical_repost
                JOIN posts historical_post ON historical_post.id = historical_repost.post_id
                WHERE historical_repost.user_id = :viewerId
                GROUP BY historical_post.author_id
            ),
            current_comments AS (
                SELECT post_id, COUNT(*) AS interaction_count
                FROM comments
                WHERE user_id = :viewerId AND status = 'PUBLISHED'
                GROUP BY post_id
            ),
            liked_hashtags AS (
                SELECT relation.hashtag_id, COUNT(*) AS interaction_count
                FROM post_likes interaction
                JOIN post_hashtags relation ON relation.post_id = interaction.post_id
                WHERE interaction.user_id = :viewerId
                GROUP BY relation.hashtag_id
            ),
            commented_hashtags AS (
                SELECT relation.hashtag_id, COUNT(*) AS interaction_count
                FROM comments interaction
                JOIN post_hashtags relation ON relation.post_id = interaction.post_id
                WHERE interaction.user_id = :viewerId AND interaction.status = 'PUBLISHED'
                GROUP BY relation.hashtag_id
            ),
            saved_hashtags AS (
                SELECT relation.hashtag_id, COUNT(*) AS interaction_count
                FROM saved_posts interaction
                JOIN post_hashtags relation ON relation.post_id = interaction.post_id
                WHERE interaction.user_id = :viewerId
                GROUP BY relation.hashtag_id
            ),
            reposted_hashtags AS (
                SELECT relation.hashtag_id, COUNT(*) AS interaction_count
                FROM post_reposts interaction
                JOIN post_hashtags relation ON relation.post_id = interaction.post_id
                WHERE interaction.user_id = :viewerId
                GROUP BY relation.hashtag_id
            ),
            scored_posts AS (
                SELECT candidate.post_id, candidate.published_at,
                       (
                           CASE
                               WHEN candidate.published_at >= DATE_SUB(:rankingAt, INTERVAL 6 HOUR) THEN 60
                               WHEN candidate.published_at >= DATE_SUB(:rankingAt, INTERVAL 1 DAY) THEN 50
                               WHEN candidate.published_at >= DATE_SUB(:rankingAt, INTERVAL 3 DAY) THEN 35
                               WHEN candidate.published_at >= DATE_SUB(:rankingAt, INTERVAL 7 DAY) THEN 20
                               WHEN candidate.published_at >= DATE_SUB(:rankingAt, INTERVAL 14 DAY) THEN 10
                               ELSE 0
                           END
                           + LEAST(candidate.like_count, 20)
                           + LEAST(candidate.comment_count, 10) * 2
                           + LEAST(candidate.repost_count, 10) * 2
                           + IF(follow_relation.following_id IS NULL, 0, 30)
                           + IF(active_school.id IS NULL, 0, 10)
                           + IF(active_faculty.id IS NULL, 0, 8)
                           + IF(active_major.id IS NULL, 0, 7)
                           + IF(viewer.entry_year IS NOT NULL AND viewer.entry_year = author_profile.entry_year, 3, 0)
                           + COALESCE(common_interest.interest_score, 0)
                           + LEAST(GREATEST(COALESCE(author_likes.interaction_count, 0)
                                   - IF(current_like.post_id IS NULL, 0, 1), 0), 3) * 2
                           + LEAST(GREATEST(COALESCE(author_comments.interaction_count, 0)
                                   - COALESCE(current_comment.interaction_count, 0), 0), 3) * 3
                           + LEAST(GREATEST(COALESCE(author_saves.interaction_count, 0)
                                   - IF(current_save.post_id IS NULL, 0, 1), 0), 2) * 4
                           + LEAST(GREATEST(COALESCE(author_reposts.interaction_count, 0)
                                   - IF(current_repost.post_id IS NULL, 0, 1), 0), 2) * 4
                           + IF(COALESCE(hashtag_likes.interaction_count, 0)
                                   - IF(current_like.post_id IS NULL, 0, 1) > 0, 3, 0)
                           + IF(COALESCE(hashtag_comments.interaction_count, 0)
                                   - COALESCE(current_comment.interaction_count, 0) > 0, 4, 0)
                           + IF(COALESCE(hashtag_saves.interaction_count, 0)
                                   - IF(current_save.post_id IS NULL, 0, 1) > 0, 5, 0)
                           + IF(COALESCE(hashtag_reposts.interaction_count, 0)
                                   - IF(current_repost.post_id IS NULL, 0, 1) > 0, 5, 0)
                       ) AS score
                FROM candidate_posts candidate
                CROSS JOIN viewer_profile viewer
                JOIN user_profiles author_profile ON author_profile.user_id = candidate.author_id
                LEFT JOIN follows follow_relation
                  ON follow_relation.follower_id = :viewerId
                 AND follow_relation.following_id = candidate.author_id
                LEFT JOIN schools active_school
                  ON active_school.id = author_profile.school_id
                 AND active_school.id = viewer.school_id
                 AND active_school.status = 'ACTIVE'
                LEFT JOIN faculties active_faculty
                  ON active_faculty.id = author_profile.faculty_id
                 AND active_faculty.id = viewer.faculty_id
                 AND active_faculty.status = 'ACTIVE'
                LEFT JOIN majors active_major
                  ON active_major.id = author_profile.major_id
                 AND active_major.id = viewer.major_id
                 AND active_major.status = 'ACTIVE'
                LEFT JOIN common_interests common_interest ON common_interest.author_id = candidate.author_id
                LEFT JOIN liked_authors author_likes ON author_likes.author_id = candidate.author_id
                LEFT JOIN commented_authors author_comments ON author_comments.author_id = candidate.author_id
                LEFT JOIN saved_authors author_saves ON author_saves.author_id = candidate.author_id
                LEFT JOIN reposted_authors author_reposts ON author_reposts.author_id = candidate.author_id
                LEFT JOIN post_likes current_like
                  ON current_like.user_id = :viewerId AND current_like.post_id = candidate.post_id
                LEFT JOIN current_comments current_comment ON current_comment.post_id = candidate.post_id
                LEFT JOIN saved_posts current_save
                  ON current_save.user_id = :viewerId AND current_save.post_id = candidate.post_id
                LEFT JOIN post_reposts current_repost
                  ON current_repost.user_id = :viewerId AND current_repost.post_id = candidate.post_id
                LEFT JOIN post_hashtags candidate_hashtag ON candidate_hashtag.post_id = candidate.post_id
                LEFT JOIN liked_hashtags hashtag_likes ON hashtag_likes.hashtag_id = candidate_hashtag.hashtag_id
                LEFT JOIN commented_hashtags hashtag_comments ON hashtag_comments.hashtag_id = candidate_hashtag.hashtag_id
                LEFT JOIN saved_hashtags hashtag_saves ON hashtag_saves.hashtag_id = candidate_hashtag.hashtag_id
                LEFT JOIN reposted_hashtags hashtag_reposts ON hashtag_reposts.hashtag_id = candidate_hashtag.hashtag_id
            )
            SELECT post_id AS postId, score AS score, published_at AS publishedAt
            FROM scored_posts
            WHERE score < :cursorScore
               OR (score = :cursorScore AND published_at < :cursorPublishedAt)
               OR (score = :cursorScore AND published_at = :cursorPublishedAt AND post_id < :cursorPostId)
            ORDER BY score DESC, published_at DESC, post_id DESC
            """, nativeQuery = true)
    List<PersonalizedPostRankProjection> findRankedPosts(
            @Param("viewerId") Long viewerId,
            @Param("rankingAt") LocalDateTime rankingAt,
            @Param("cursorScore") int cursorScore,
            @Param("cursorPublishedAt") LocalDateTime cursorPublishedAt,
            @Param("cursorPostId") Long cursorPostId,
            Pageable limit
    );
}
