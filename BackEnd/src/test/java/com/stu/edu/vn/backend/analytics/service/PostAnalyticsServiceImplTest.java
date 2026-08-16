package com.stu.edu.vn.backend.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.analytics.repository.PostAnalyticsRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostAnalyticsServiceImplTest {
    private PostAnalyticsRepository repository;
    private PostAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PostAnalyticsRepository.class);
        service = new PostAnalyticsServiceImpl(repository,
                Clock.fixed(Instant.parse("2026-08-16T10:00:00Z"), ZoneOffset.UTC));
        when(repository.countStatuses()).thenReturn(Map.of("PUBLISHED", 8L, "HIDDEN", 1L, "DELETED", 1L));
        when(repository.countPosts(any(), any())).thenReturn(2L, 1L);
        when(repository.countInteractions(any(), any())).thenReturn(12L, 6L);
        when(repository.summarizeInteractions(any(), any())).thenReturn(Map.of("likes", 5L, "comments", 3L, "saves", 2L, "reposts", 2L));
        when(repository.countDailyPosts(any(), any())).thenReturn(Map.of(LocalDate.of(2026, 8, 16), 2L));
        when(repository.findTopPosts(any(), any())).thenReturn(List.of());
        when(repository.summarizeModeration(any(), any())).thenReturn(Map.of("OPEN", 1L, "RESOLVED_ACTION_TAKEN", 1L));
        when(repository.findMostReported(any(), any())).thenReturn(List.of());
    }

    @Test
    void sevenDayPresetFillsMissingDaysAndComparesPreviousPeriod() {
        // Biểu đồ luôn đủ điểm ngày, kể cả ngày không phát sinh bài viết.
        var result = service.getAnalytics("7D", null, null);

        assertThat(result.fromDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(result.toDate()).isEqualTo(LocalDate.of(2026, 8, 16));
        assertThat(result.trend()).hasSize(7);
        assertThat(result.trend().getLast().count()).isEqualTo(2);
        assertThat(result.kpis().newPostsChangeRate()).isEqualByComparingTo("100.0");
        assertThat(result.interactions().averagePerPost()).isEqualByComparingTo("6.0");
        assertThat(result.moderation().violationRate()).isEqualByComparingTo("50.0");
    }

    @Test
    void rejectsCustomRangeLongerThanOneYear() {
        assertThatThrownBy(() -> service.getAnalytics("30D", "2025-01-01", "2026-08-16"))
                .isInstanceOf(BusinessException.class);
    }
}
