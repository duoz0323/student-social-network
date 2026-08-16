package com.stu.edu.vn.backend.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Dữ liệu tổng hợp duy nhất cho toàn bộ màn hình thống kê bài viết quản trị. */
public record PostAnalyticsResponse(
        LocalDate fromDate,
        LocalDate toDate,
        Kpis kpis,
        StatusDistribution statusDistribution,
        List<TrendPoint> trend,
        InteractionSummary interactions,
        List<RankedPost> topPosts,
        ModerationSummary moderation,
        List<ReportedPost> mostReportedPosts
) {
    public record Kpis(long totalPosts, long publishedPosts, long newPosts, BigDecimal newPostsChangeRate,
                       long hiddenPosts, long deletedPosts, long totalInteractions,
                       BigDecimal interactionsChangeRate) {}

    public record StatusDistribution(long published, long hidden, long deleted) {}

    public record TrendPoint(LocalDate date, long count) {}

    public record InteractionSummary(long likes, long comments, long saves, long reposts,
                                     long total, BigDecimal averagePerPost) {}

    public record Author(long id, String displayName, String username, String avatarUrl) {}

    public record RankedPost(long postId, String contentPreview, String thumbnailUrl, Author author,
                             long likes, long comments, long saves, long reposts, long totalInteractions) {}

    public record ModerationSummary(long open, long resolvedActionTaken, long resolvedNoViolation,
                                    BigDecimal violationRate) {}

    public record ReportedPost(long caseId, long postId, String contentPreview, String thumbnailUrl,
                               Author author, long reportCount, String postStatus, String caseStatus) {}
}
