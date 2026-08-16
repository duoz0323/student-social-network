package com.stu.edu.vn.backend.admin.collaborator.analytics;

import com.stu.edu.vn.backend.admin.collaborator.identity.CollaboratorSocialIdentityResolver;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Analytics đọc trực tiếp dữ liệu nghiệp vụ có timestamp; không nội suy hoặc tạo số liệu giả. */
@Service
@RequiredArgsConstructor
public class CollaboratorAnalyticsService {
    private static final int MAX_SIZE = 100;
    private final NamedParameterJdbcTemplate jdbc;
    private final CollaboratorSocialIdentityResolver identityResolver;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse<CollaboratorPostListItem> posts(
            String keyword, String status, String sort, int page, int size) {
        validatePage(page, size);
        Long actorId = actorId();
        String normalizedStatus = normalizeStatus(status);
        String normalizedKeyword = text(keyword);
        String order = switch (sort == null ? "NEWEST" : sort.trim().toUpperCase(Locale.ROOT)) {
            case "NEWEST" -> "p.created_at DESC, p.id DESC";
            case "OLDEST" -> "p.created_at ASC, p.id ASC";
            case "MOST_LIKED" -> "p.like_count DESC, p.created_at DESC, p.id DESC";
            case "MOST_COMMENTED" -> "p.comment_count DESC, p.created_at DESC, p.id DESC";
            default -> throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        };
        Map<String, Object> params = new HashMap<>();
        params.put("authorId", actorId);
        params.put("keyword", normalizedKeyword);
        params.put("status", normalizedStatus);
        params.put("limit", size);
        params.put("offset", page * size);
        String where = " WHERE p.author_id=:authorId AND (:keyword IS NULL OR p.content LIKE CONCAT('%',:keyword,'%')) "
                + "AND (:status IS NULL OR p.status=:status) ";
        long total = Objects.requireNonNull(jdbc.queryForObject(
                "SELECT COUNT(*) FROM posts p" + where, params, Long.class));
        List<CollaboratorPostListItem> content = jdbc.query(baseSelect() + where + " ORDER BY " + order
                + " LIMIT :limit OFFSET :offset", params, (rs, row) -> mapPost(rs));
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponse<>(content, page, size, total, totalPages, page == 0, page + 1 >= totalPages);
    }

    @Transactional(readOnly = true)
    public CollaboratorDashboardResponse dashboard(int days) {
        if (days != 7 && days != 30 && days != 90) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        Long actorId = actorId();
        LocalDateTime from = LocalDateTime.now(clock).minusDays(days);
        Map<String, Object> params = Map.of("authorId", actorId, "from", from, "limit", 5);
        Map<String, Object> totals = jdbc.queryForMap("""
                SELECT COUNT(*) totalPosts, COALESCE(SUM(like_count),0) totalLikes,
                       COALESCE(SUM(comment_count),0) totalComments, COALESCE(SUM(repost_count),0) totalReposts
                FROM posts WHERE author_id=:authorId AND created_at>=:from AND status<>'DELETED'
                """, params);
        String where = " WHERE p.author_id=:authorId AND p.created_at>=:from AND p.status<>'DELETED' ";
        List<CollaboratorPostListItem> recent = jdbc.query(baseSelect() + where
                + "ORDER BY p.created_at DESC,p.id DESC LIMIT :limit", params, (rs, row) -> mapPost(rs));
        List<CollaboratorPostListItem> top = jdbc.query(baseSelect() + where
                + "ORDER BY (p.like_count+p.comment_count+p.repost_count) DESC,p.id DESC LIMIT :limit",
                params, (rs, row) -> mapPost(rs));
        return new CollaboratorDashboardResponse(number(totals, "totalPosts"), number(totals, "totalLikes"),
                number(totals, "totalComments"), number(totals, "totalReposts"), recent, top,
                trend(actorId, null, from));
    }

    @Transactional(readOnly = true)
    public CollaboratorPostAnalyticsResponse analytics(Long postId, String range) {
        Long actorId = actorId();
        int hours = switch (range == null ? "7D" : range.trim().toUpperCase(Locale.ROOT)) {
            case "24H" -> 24; case "7D" -> 24 * 7; case "30D" -> 24 * 30;
            default -> throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        };
        Map<String, Object> params = Map.of("authorId", actorId, "postId", postId);
        List<CollaboratorPostListItem> posts = jdbc.query(baseSelect()
                + " WHERE p.author_id=:authorId AND p.id=:postId", params, (rs, row) -> mapPost(rs));
        if (posts.isEmpty()) throw new BusinessException(ErrorCode.COLLABORATOR_POST_NOT_OWNED);
        CollaboratorPostListItem post = posts.get(0);
        long total = (long) post.likeCount() + post.commentCount() + post.repostCount();
        return new CollaboratorPostAnalyticsResponse(post, post.likeCount(), post.commentCount(), post.repostCount(),
                total, trend(actorId, postId, LocalDateTime.now(clock).minusHours(hours)));
    }

    private List<InteractionTrendPoint> trend(Long authorId, Long postId, LocalDateTime from) {
        Map<String, Object> params = new HashMap<>();
        params.put("authorId", authorId); params.put("postId", postId); params.put("from", from);
        return jdbc.query("""
                SELECT day, SUM(likes) likes, SUM(comments) comments, SUM(reposts) reposts FROM (
                  SELECT DATE(pl.created_at) day, COUNT(*) likes, 0 comments, 0 reposts
                  FROM post_likes pl JOIN posts p ON p.id=pl.post_id
                  WHERE p.author_id=:authorId AND pl.created_at>=:from AND (:postId IS NULL OR p.id=:postId) GROUP BY DATE(pl.created_at)
                  UNION ALL
                  SELECT DATE(c.created_at),0,COUNT(*),0 FROM comments c JOIN posts p ON p.id=c.post_id
                  WHERE p.author_id=:authorId AND c.created_at>=:from AND c.status='PUBLISHED' AND (:postId IS NULL OR p.id=:postId) GROUP BY DATE(c.created_at)
                  UNION ALL
                  SELECT DATE(pr.created_at),0,0,COUNT(*) FROM post_reposts pr JOIN posts p ON p.id=pr.post_id
                  WHERE p.author_id=:authorId AND pr.created_at>=:from AND (:postId IS NULL OR p.id=:postId) GROUP BY DATE(pr.created_at)
                ) x GROUP BY day ORDER BY day
                """, params, (rs, row) -> new InteractionTrendPoint(rs.getDate("day").toLocalDate(),
                rs.getLong("likes"), rs.getLong("comments"), rs.getLong("reposts")));
    }

    private String baseSelect() {
        return """
                SELECT p.id,p.content,p.created_at,p.updated_at,p.published_at,p.like_count,p.comment_count,
                       p.repost_count,p.status,
                       (SELECT pm.media_url FROM post_media pm WHERE pm.post_id=p.id ORDER BY pm.display_order,pm.id LIMIT 1) thumbnail,
                       (SELECT h.normalized_name FROM post_hashtags ph JOIN hashtags h ON h.id=ph.hashtag_id WHERE ph.post_id=p.id LIMIT 1) hashtag
                FROM posts p
                """;
    }

    private CollaboratorPostListItem mapPost(java.sql.ResultSet rs) throws java.sql.SQLException {
        LocalDateTime published = rs.getTimestamp("published_at").toLocalDateTime();
        LocalDateTime deadline = published.plusMinutes(15);
        String status = rs.getString("status");
        String content = rs.getString("content");
        return new CollaboratorPostListItem(rs.getLong("id"), preview(content), rs.getString("thumbnail"),
                rs.getString("hashtag"), time(rs.getTimestamp("created_at")), time(rs.getTimestamp("updated_at")),
                rs.getInt("like_count"), rs.getInt("comment_count"), rs.getInt("repost_count"), status,
                "PUBLISHED".equals(status) && LocalDateTime.now(clock).isBefore(deadline), deadline);
    }

    private Long actorId() {
        return identityResolver.resolveActive(currentUserProvider.getCurrentUserId()).getId();
    }
    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_SIZE) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
    private String normalizeStatus(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)) return null;
        String status = value.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("PUBLISHED","HIDDEN","DELETED").contains(status)) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        return status;
    }
    private String text(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String preview(String value) { return value == null ? "" : value.substring(0, Math.min(160, value.length())); }
    private LocalDateTime time(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }
    private long number(Map<String,Object> row, String key) { return ((Number) row.get(key)).longValue(); }
}
