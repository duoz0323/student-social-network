package com.stu.edu.vn.backend.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

class PersonalizedFeedRepositoryContractTest {

    private String queryText() throws Exception {
        Method method = PersonalizedFeedRepository.class.getMethod(
                "findRankedPosts", Long.class, LocalDateTime.class, int.class,
                LocalDateTime.class, Long.class, Pageable.class);
        return method.getAnnotation(Query.class).value();
    }

    @Test
    void candidateAccessRulesAreAppliedBeforeScoring() throws Exception {
        String query = queryText();

        assertThat(query).contains(
                "p.status = 'PUBLISHED'",
                "author.status = 'ACTIVE'",
                "author_profile.profile_completed_at IS NOT NULL",
                "blocked_relation.blocker_id = :viewerId",
                "blocked_relation.blocked_id = :viewerId"
        );
        assertThat(query).doesNotContain("user_restrictions", "OFFSET", "ORDER BY RAND", "COUNT(*) OVER");
    }

    @Test
    void deterministicFormulaContainsEveryPersonalizationSignalAndCap() throws Exception {
        String query = queryText();

        assertThat(query).contains(
                "DATE_SUB(:rankingAt, INTERVAL 6 HOUR) THEN 60",
                "DATE_SUB(:rankingAt, INTERVAL 1 DAY) THEN 50",
                "DATE_SUB(:rankingAt, INTERVAL 3 DAY) THEN 35",
                "DATE_SUB(:rankingAt, INTERVAL 7 DAY) THEN 20",
                "DATE_SUB(:rankingAt, INTERVAL 14 DAY) THEN 10",
                "LEAST(candidate.like_count, 20)",
                "LEAST(candidate.comment_count, 10) * 2",
                "LEAST(candidate.repost_count, 10) * 2",
                "IF(follow_relation.following_id IS NULL, 0, 30)",
                "IF(active_school.id IS NULL, 0, 10)",
                "IF(active_faculty.id IS NULL, 0, 8)",
                "IF(active_major.id IS NULL, 0, 7)",
                "viewer.entry_year = author_profile.entry_year, 3, 0",
                "LEAST(COUNT(*), 5) * 2 AS interest_score",
                "liked_authors",
                "commented_authors",
                "saved_authors",
                "reposted_authors",
                "liked_hashtags",
                "commented_hashtags",
                "saved_hashtags",
                "reposted_hashtags"
        );
    }

    @Test
    void candidatePostIsSubtractedFromHistoryAndCursorUsesAllRankKeys() throws Exception {
        String query = queryText();

        assertThat(query).contains(
                "- IF(current_like.post_id IS NULL, 0, 1)",
                "- COALESCE(current_comment.interaction_count, 0)",
                "- IF(current_save.post_id IS NULL, 0, 1)",
                "- IF(current_repost.post_id IS NULL, 0, 1)",
                "score < :cursorScore",
                "published_at < :cursorPublishedAt",
                "post_id < :cursorPostId",
                "ORDER BY score DESC, published_at DESC, post_id DESC"
        );
    }
}
