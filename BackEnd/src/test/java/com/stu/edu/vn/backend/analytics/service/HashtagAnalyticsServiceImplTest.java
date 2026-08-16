package com.stu.edu.vn.backend.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.analytics.repository.HashtagAnalyticsRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HashtagAnalyticsServiceImplTest {
    private HashtagAnalyticsRepository repository;
    private HashtagAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(HashtagAnalyticsRepository.class);
        service = new HashtagAnalyticsServiceImpl(repository,
                Clock.fixed(Instant.parse("2026-08-16T10:00:00Z"), ZoneOffset.UTC));
        when(repository.summarizeKpis(any(), any())).thenReturn(
                new HashtagAnalyticsRepository.KpiCounts(12, 4, 4, 20, 8));
        when(repository.countNewHashtags(any(), any())).thenReturn(2L);
        when(repository.findTrend(any(), any(), anyBoolean())).thenReturn(List.of(
                new HashtagAnalyticsRepository.TrendCount("2026-08-16", 3, 5)));
        when(repository.findPopular(any(), any())).thenReturn(List.of(
                new HashtagAnalyticsRepository.HashtagCount(1, "hoctap", 3)));
        when(repository.findGrowth(any(), any(), any())).thenReturn(List.of(
                new HashtagAnalyticsRepository.GrowthCount(1, "hoctap", 2, 3)));
        when(repository.findRecent(any(), any())).thenReturn(List.of(
                new HashtagAnalyticsRepository.RecentCount(1, "hoctap", 8, 3,
                        LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 8, 16, 9, 0))));
        when(repository.findLowUsage()).thenReturn(List.of(
                new HashtagAnalyticsRepository.HashtagCount(2, "java21", 1)));
    }

    @Test
    void sevenDayPresetFillsTrendAndCalculatesKpis() {
        // Chuỗi ngày phải được bù đủ để biểu đồ không bị đứt ở ngày không có bài.
        var result = service.getAnalytics("7D", null, null);

        assertThat(result.fromDate().toString()).isEqualTo("2026-08-10");
        assertThat(result.trend()).hasSize(7);
        assertThat(result.trend().getLast().postsWithHashtag()).isEqualTo(3);
        assertThat(result.kpis().usageRate()).isEqualByComparingTo("40.0");
        assertThat(result.kpis().averagePostsPerUsedHashtag()).isEqualByComparingTo("2.0");
        assertThat(result.kpis().newHashtagsChangeRate()).isEqualByComparingTo("100.0");
        assertThat(result.popularHashtags().getFirst().share()).isEqualByComparingTo("37.5");
    }

    @Test
    void rejectsFutureOrUnsupportedRange() {
        assertThatThrownBy(() -> service.getAnalytics("2Y", null, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.getAnalytics("30D", "2026-08-01", "2026-08-17"))
                .isInstanceOf(BusinessException.class);
    }
}
