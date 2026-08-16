package com.stu.edu.vn.backend.analytics.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Tổng hợp hashtag trực tiếp tại MySQL, không tải toàn bộ danh sách về Java. */
@Repository
public class HashtagAnalyticsRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public HashtagAnalyticsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public KpiCounts summarizeKpis(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM hashtags) total_hashtags,
                  (SELECT COUNT(DISTINCT ph.hashtag_id) FROM post_hashtags ph
                     JOIN posts p ON p.id = ph.post_id
                    WHERE p.created_at >= :from AND p.created_at < :to) used_hashtags,
                  (SELECT COUNT(*) FROM hashtags h
                    WHERE h.created_at >= :from AND h.created_at < :to) new_hashtags,
                  (SELECT COUNT(*) FROM posts p
                    WHERE p.created_at >= :from AND p.created_at < :to) total_posts,
                  (SELECT COUNT(*) FROM posts p JOIN post_hashtags ph ON ph.post_id = p.id
                    WHERE p.created_at >= :from AND p.created_at < :to) tagged_posts
                """;
        return jdbc.queryForObject(sql, params(from, toExclusive), (rs, row) -> new KpiCounts(
                rs.getLong("total_hashtags"), rs.getLong("used_hashtags"),
                rs.getLong("new_hashtags"), rs.getLong("total_posts"), rs.getLong("tagged_posts")));
    }

    public long countNewHashtags(LocalDateTime from, LocalDateTime toExclusive) {
        Long value = jdbc.queryForObject("SELECT COUNT(*) FROM hashtags WHERE created_at >= :from AND created_at < :to",
                params(from, toExclusive), Long.class);
        return value == null ? 0 : value;
    }

    public List<TrendCount> findTrend(LocalDateTime from, LocalDateTime toExclusive, boolean monthly) {
        String period = monthly ? "DATE_FORMAT(p.created_at, '%Y-%m')" : "DATE_FORMAT(p.created_at, '%Y-%m-%d')";
        String sql = "SELECT " + period + " period, COUNT(*) total_posts, COUNT(ph.post_id) tagged_posts "
                + "FROM posts p LEFT JOIN post_hashtags ph ON ph.post_id = p.id "
                + "WHERE p.created_at >= :from AND p.created_at < :to GROUP BY " + period + " ORDER BY period";
        return jdbc.query(sql, params(from, toExclusive), (rs, row) -> new TrendCount(
                rs.getString("period"), rs.getLong("tagged_posts"), rs.getLong("total_posts")));
    }

    public List<HashtagCount> findPopular(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = """
                SELECT h.id, h.display_name, COUNT(*) period_count
                FROM hashtags h JOIN post_hashtags ph ON ph.hashtag_id = h.id
                JOIN posts p ON p.id = ph.post_id
                WHERE p.created_at >= :from AND p.created_at < :to
                GROUP BY h.id, h.display_name
                ORDER BY period_count DESC, h.id DESC LIMIT 10
                """;
        return jdbc.query(sql, params(from, toExclusive), (rs, row) -> new HashtagCount(
                rs.getLong("id"), rs.getString("display_name"), rs.getLong("period_count")));
    }

    public List<GrowthCount> findGrowth(LocalDateTime previousFrom, LocalDateTime currentFrom,
                                         LocalDateTime currentToExclusive) {
        String sql = """
                SELECT h.id, h.display_name,
                  SUM(CASE WHEN p.created_at >= :previousFrom AND p.created_at < :currentFrom THEN 1 ELSE 0 END) previous_count,
                  SUM(CASE WHEN p.created_at >= :currentFrom AND p.created_at < :currentTo THEN 1 ELSE 0 END) current_count
                FROM hashtags h JOIN post_hashtags ph ON ph.hashtag_id = h.id
                JOIN posts p ON p.id = ph.post_id
                WHERE p.created_at >= :previousFrom AND p.created_at < :currentTo
                GROUP BY h.id, h.display_name
                ORDER BY (current_count - previous_count) DESC, current_count DESC, h.id DESC LIMIT 10
                """;
        var parameters = new MapSqlParameterSource()
                .addValue("previousFrom", previousFrom).addValue("currentFrom", currentFrom)
                .addValue("currentTo", currentToExclusive);
        return jdbc.query(sql, parameters, (rs, row) -> new GrowthCount(
                rs.getLong("id"), rs.getString("display_name"),
                rs.getLong("previous_count"), rs.getLong("current_count")));
    }

    public List<RecentCount> findRecent(LocalDateTime from, LocalDateTime toExclusive) {
        String sql = """
                SELECT h.id, h.display_name, h.post_count, h.created_at,
                  SUM(CASE WHEN p.created_at >= :from AND p.created_at < :to THEN 1 ELSE 0 END) period_count,
                  MAX(ph.created_at) latest_used_at
                FROM hashtags h JOIN post_hashtags ph ON ph.hashtag_id = h.id
                JOIN posts p ON p.id = ph.post_id
                GROUP BY h.id, h.display_name, h.post_count, h.created_at
                ORDER BY latest_used_at DESC, h.id DESC LIMIT 10
                """;
        return jdbc.query(sql, params(from, toExclusive), (rs, row) -> new RecentCount(
                rs.getLong("id"), rs.getString("display_name"), rs.getLong("post_count"),
                rs.getLong("period_count"), rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("latest_used_at").toLocalDateTime()));
    }

    public List<HashtagCount> findLowUsage() {
        String sql = "SELECT id, display_name, post_count period_count FROM hashtags "
                + "ORDER BY post_count ASC, id DESC LIMIT 5";
        return jdbc.query(sql, (rs, row) -> new HashtagCount(
                rs.getLong("id"), rs.getString("display_name"), rs.getLong("period_count")));
    }

    private MapSqlParameterSource params(LocalDateTime from, LocalDateTime toExclusive) {
        return new MapSqlParameterSource().addValue("from", from).addValue("to", toExclusive);
    }

    public record KpiCounts(long totalHashtags, long usedHashtags, long newHashtags,
                            long totalPosts, long taggedPosts) {}
    public record TrendCount(String period, long taggedPosts, long totalPosts) {}
    public record HashtagCount(long id, String name, long count) {}
    public record GrowthCount(long id, String name, long previousCount, long currentCount) {}
    public record RecentCount(long id, String name, long linkedPostCount, long periodPostCount,
                              LocalDateTime createdAt, LocalDateTime latestUsedAt) {}
}
