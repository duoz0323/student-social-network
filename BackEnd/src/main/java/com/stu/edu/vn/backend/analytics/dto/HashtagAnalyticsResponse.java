package com.stu.edu.vn.backend.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Dữ liệu tổng hợp cho toàn bộ màn hình thống kê hashtag quản trị. */
public record HashtagAnalyticsResponse(
        LocalDate fromDate,
        LocalDate toDate,
        String granularity,
        Kpis kpis,
        List<TrendPoint> trend,
        List<PopularHashtag> popularHashtags,
        Distribution distribution,
        List<GrowthHashtag> growthHashtags,
        List<RecentHashtag> recentHashtags,
        List<LowUsageHashtag> lowUsageHashtags
) {
    public record Kpis(long totalHashtags, long usedHashtags, long newHashtags,
                       BigDecimal newHashtagsChangeRate, long postsWithHashtag,
                       long totalPosts, BigDecimal usageRate, BigDecimal averagePostsPerUsedHashtag) {}

    public record TrendPoint(String period, long postsWithHashtag, long totalPosts) {}

    public record PopularHashtag(long hashtagId, String name, long postCount, BigDecimal share) {}

    public record Distribution(long topTenPosts, long otherPosts) {}

    public record GrowthHashtag(long hashtagId, String name, long previousCount,
                                long currentCount, BigDecimal changeRate) {}

    public record RecentHashtag(long hashtagId, String name, long linkedPostCount,
                                long periodPostCount, LocalDateTime createdAt,
                                LocalDateTime latestUsedAt) {}

    public record LowUsageHashtag(long hashtagId, String name, long linkedPostCount) {}
}
