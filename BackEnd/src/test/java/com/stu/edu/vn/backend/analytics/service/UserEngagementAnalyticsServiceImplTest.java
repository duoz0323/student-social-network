package com.stu.edu.vn.backend.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.analytics.repository.DailyInteractionCount;
import com.stu.edu.vn.backend.analytics.repository.FeaturedUserEngagement;
import com.stu.edu.vn.backend.analytics.repository.MonthlyUserEngagementCounts;
import com.stu.edu.vn.backend.analytics.repository.UserEngagementAnalyticsRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserEngagementAnalyticsServiceImplTest {

    private final UserEngagementAnalyticsRepository repository =
            org.mockito.Mockito.mock(UserEngagementAnalyticsRepository.class);
    private UserEngagementAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
        service = new UserEngagementAnalyticsServiceImpl(repository, clock);
    }

    @Test
    void calculatesAllCountsRatesAndCurrentEvaluationDate() {
        // 20 eligible = 3 NEW + 5 REGULAR + 2 RETURNING + 2 RECENT + 4 INACTIVE + 4 NEVER.
        when(repository.summarizeMonth(any(), any(), eq(15)))
                .thenReturn(new MonthlyUserEngagementCounts(20, 3, 5, 2, 2, 4, 4, 1));

        var item = service.getSummary("2026-06", 15);

        assertThat(item.evaluationDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(item.activeUserCount()).isEqualTo(10);
        assertThat(item.inactiveUserCount()).isEqualTo(10);
        assertThat(item.eligibleInactiveUserCount()).isEqualTo(5);
        assertThat(item.activeUserRate()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(item.regularActiveRate()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(item.returnRate()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(item.neverActiveRate()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(item.eligibleSystemUserCount())
                .isEqualTo(item.activeUserCount() + item.inactiveUserCount());
    }

    @Test
    void dashboardFillsMissingUtcDaysAndMapsFeaturedUsers() {
        when(repository.findDailyInteractionCounts(
                LocalDate.of(2026, 6, 13), LocalDate.of(2026, 6, 15)))
                .thenReturn(List.of(
                        new DailyInteractionCount(LocalDate.of(2026, 6, 13), 4),
                        new DailyInteractionCount(LocalDate.of(2026, 6, 15), 9)
                ));
        when(repository.findFeaturedUsers(
                LocalDate.of(2026, 6, 15),
                LocalDateTime.of(2026, 6, 15, 0, 0),
                LocalDateTime.of(2026, 6, 16, 0, 0),
                5
        )).thenReturn(List.of(new FeaturedUserEngagement(7L, "Mai", "avatar", 3, 9)));

        var dashboard = service.getDashboard(3);

        assertThat(dashboard.fromDate()).isEqualTo(LocalDate.of(2026, 6, 13));
        assertThat(dashboard.toDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(dashboard.dailyInteractions()).extracting(item -> item.interactionCount())
                .containsExactly(4L, 0L, 9L);
        assertThat(dashboard.featuredUsers()).singleElement().satisfies(user -> {
            assertThat(user.userId()).isEqualTo(7L);
            assertThat(user.postCount()).isEqualTo(3L);
            assertThat(user.interactionCount()).isEqualTo(9L);
        });
    }

    @Test
    void returnsNullRatesWhenTheirDenominatorsAreZero() {
        when(repository.summarizeMonth(any(), any(), eq(15)))
                .thenReturn(new MonthlyUserEngagementCounts(0, 0, 0, 0, 0, 0, 0, 0));

        var item = service.getSummary("2026-05", 15);

        assertThat(item.activeUserRate()).isNull();
        assertThat(item.regularActiveRate()).isNull();
        assertThat(item.returnRate()).isNull();
        assertThat(item.neverActiveRate()).isNull();
        assertThat(item.evaluationDate()).isEqualTo(LocalDate.of(2026, 5, 31));
    }

    @Test
    void monthlyIncludesEmptyMonthsAndSelectsEarliestPeakOnTie() {
        when(repository.summarizeMonth(any(), any(), eq(15)))
                .thenReturn(new MonthlyUserEngagementCounts(2, 0, 0, 2, 0, 0, 0, 1))
                .thenReturn(new MonthlyUserEngagementCounts(4, 0, 1, 2, 0, 1, 0, 1));

        var response = service.getMonthly("2026-04", "2026-05", 15);

        assertThat(response.items()).hasSize(2);
        assertThat(response.peakReturningMonth().toString()).isEqualTo("2026-04");
        assertThat(response.peakReturningUserCount()).isEqualTo(2);
        assertThat(response.peakReturnRateMonth().toString()).isEqualTo("2026-04");
        assertThat(response.comparisonOperator()).isEqualTo("GREATER_THAN");
    }

    @Test
    void rejectsInvalidMonthRangeFutureMonthInactiveDaysAndExcessiveRange() {
        assertError(() -> service.getMonthly("2026-1", "2026-06", 15), ErrorCode.ANALYTICS_MONTH_INVALID);
        assertError(() -> service.getMonthly("2026-06", "2026-05", 15), ErrorCode.ANALYTICS_MONTH_RANGE_INVALID);
        assertError(() -> service.getMonthly("2026-06", "2026-07", 15), ErrorCode.ANALYTICS_FUTURE_MONTH);
        assertError(() -> service.getMonthly("2024-07", "2026-06", 0), ErrorCode.ANALYTICS_INACTIVE_DAYS_INVALID);
        assertError(() -> service.getMonthly("2024-05", "2026-05", 15), ErrorCode.ANALYTICS_MONTH_RANGE_TOO_LARGE);
        assertError(() -> service.getDashboard(0), ErrorCode.ANALYTICS_DASHBOARD_DAYS_INVALID);
        assertError(() -> service.getDashboard(91), ErrorCode.ANALYTICS_DASHBOARD_DAYS_INVALID);
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode code) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(code);
    }
}
