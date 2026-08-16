package com.stu.edu.vn.backend.analytics.repository;

import com.stu.edu.vn.backend.analytics.dto.PostAnalyticsResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Thực hiện các phép tổng hợp tại MySQL để không tải toàn bộ bài viết và tương tác vào bộ nhớ. */
@Repository
public class PostAnalyticsRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public PostAnalyticsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Long> countStatuses() {
        return jdbc.query("SELECT status, COUNT(*) total FROM posts GROUP BY status", Map.of(), rs -> {
            Map<String, Long> values = new java.util.HashMap<>();
            while (rs.next()) values.put(rs.getString("status"), rs.getLong("total"));
            return values;
        });
    }

    public long countPosts(LocalDateTime from, LocalDateTime toExclusive) {
        return count("SELECT COUNT(*) FROM posts WHERE created_at >= :from AND created_at < :to", from, toExclusive);
    }

    public long countInteractions(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = """
                SELECT COALESCE(SUM(value), 0) FROM (
                    SELECT COUNT(*) value FROM post_likes WHERE created_at >= :from AND created_at < :to
                    UNION ALL SELECT COUNT(*) FROM comments WHERE status = 'PUBLISHED' AND created_at >= :from AND created_at < :to
                    UNION ALL SELECT COUNT(*) FROM saved_posts WHERE created_at >= :from AND created_at < :to
                    UNION ALL SELECT COUNT(*) FROM post_reposts WHERE created_at >= :from AND created_at < :to
                ) interactions
                """;
        return count(sql, from, toExclusive);
    }

    public Map<String, Long> summarizeInteractions(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM post_likes WHERE created_at >= :from AND created_at < :to) likes,
                  (SELECT COUNT(*) FROM comments WHERE status = 'PUBLISHED' AND created_at >= :from AND created_at < :to) comments,
                  (SELECT COUNT(*) FROM saved_posts WHERE created_at >= :from AND created_at < :to) saves,
                  (SELECT COUNT(*) FROM post_reposts WHERE created_at >= :from AND created_at < :to) reposts
                """;
        return jdbc.queryForObject(sql, params(from, toExclusive), (rs, row) -> Map.of(
                "likes", rs.getLong("likes"), "comments", rs.getLong("comments"),
                "saves", rs.getLong("saves"), "reposts", rs.getLong("reposts")));
    }

    public Map<LocalDate, Long> countDailyPosts(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = "SELECT DATE(created_at) day, COUNT(*) total FROM posts "
                + "WHERE created_at >= :from AND created_at < :to GROUP BY DATE(created_at) ORDER BY day";
        return jdbc.query(sql, params(from, toExclusive), rs -> {
            Map<LocalDate, Long> values = new java.util.LinkedHashMap<>();
            while (rs.next()) values.put(rs.getDate("day").toLocalDate(), rs.getLong("total"));
            return values;
        });
    }

    public List<PostAnalyticsResponse.RankedPost> findTopPosts(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = """
                SELECT p.id, LEFT(COALESCE(p.content, ''), 140) content_preview,
                       (SELECT COALESCE(pm.thumbnail_url, pm.media_url) FROM post_media pm WHERE pm.post_id=p.id ORDER BY pm.display_order LIMIT 1) thumbnail_url,
                       u.id author_id, up.display_name, up.username, up.avatar_url,
                       p.like_count likes, p.comment_count comments, p.repost_count reposts,
                       (SELECT COUNT(*) FROM saved_posts sp WHERE sp.post_id=p.id) saves
                FROM posts p JOIN users u ON u.id=p.author_id JOIN user_profiles up ON up.user_id=u.id
                WHERE p.created_at >= :from AND p.created_at < :to
                ORDER BY (p.like_count+p.comment_count+p.repost_count+(SELECT COUNT(*) FROM saved_posts sp WHERE sp.post_id=p.id)) DESC, p.id DESC
                LIMIT 5
                """;
        return jdbc.query(sql, params(from, toExclusive), (rs, row) -> {
            long likes=rs.getLong("likes"), comments=rs.getLong("comments"), saves=rs.getLong("saves"), reposts=rs.getLong("reposts");
            var author = new PostAnalyticsResponse.Author(rs.getLong("author_id"), rs.getString("display_name"), rs.getString("username"), rs.getString("avatar_url"));
            return new PostAnalyticsResponse.RankedPost(rs.getLong("id"), rs.getString("content_preview"), rs.getString("thumbnail_url"), author,
                    likes, comments, saves, reposts, likes+comments+saves+reposts);
        });
    }

    public Map<String, Long> summarizeModeration(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = "SELECT status, COUNT(*) total FROM moderation_cases WHERE first_reported_at >= :from AND first_reported_at < :to GROUP BY status";
        return jdbc.query(sql, params(from, toExclusive), rs -> {
            Map<String, Long> values = new java.util.HashMap<>();
            while (rs.next()) values.put(rs.getString("status"), rs.getLong("total"));
            return values;
        });
    }

    public List<PostAnalyticsResponse.ReportedPost> findMostReported(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = """
                SELECT mc.id case_id, p.id post_id, LEFT(COALESCE(p.content, ''), 140) content_preview,
                       (SELECT COALESCE(pm.thumbnail_url, pm.media_url) FROM post_media pm WHERE pm.post_id=p.id ORDER BY pm.display_order LIMIT 1) thumbnail_url,
                       u.id author_id, up.display_name, up.username, up.avatar_url,
                       mc.report_count, p.status post_status, mc.status case_status
                FROM moderation_cases mc JOIN posts p ON p.id=mc.post_id
                JOIN users u ON u.id=p.author_id JOIN user_profiles up ON up.user_id=u.id
                WHERE mc.first_reported_at >= :from AND mc.first_reported_at < :to
                ORDER BY mc.report_count DESC, mc.latest_reported_at DESC LIMIT 5
                """;
        return jdbc.query(sql, params(from, toExclusive), (rs, row) -> new PostAnalyticsResponse.ReportedPost(
                rs.getLong("case_id"), rs.getLong("post_id"), rs.getString("content_preview"), rs.getString("thumbnail_url"),
                new PostAnalyticsResponse.Author(rs.getLong("author_id"), rs.getString("display_name"), rs.getString("username"), rs.getString("avatar_url")),
                rs.getLong("report_count"), rs.getString("post_status"), rs.getString("case_status")));
    }

    private long count(String sql, LocalDateTime from, LocalDateTime toExclusive) {
        Long value = jdbc.queryForObject(sql, params(from, toExclusive), Long.class);
        return value == null ? 0 : value;
    }

    private MapSqlParameterSource params(LocalDateTime from, LocalDateTime toExclusive) {
        return new MapSqlParameterSource().addValue("from", from).addValue("to", toExclusive);
    }
}
