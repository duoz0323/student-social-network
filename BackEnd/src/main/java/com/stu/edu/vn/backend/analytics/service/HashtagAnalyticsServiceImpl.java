package com.stu.edu.vn.backend.analytics.service;

import com.stu.edu.vn.backend.analytics.dto.HashtagAnalyticsResponse;
import com.stu.edu.vn.backend.analytics.repository.HashtagAnalyticsRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Chuẩn hóa cửa sổ UTC và tạo một snapshot nhất quán cho toàn trang hashtag analytics. */
@Service
@Transactional(readOnly = true)
public class HashtagAnalyticsServiceImpl implements HashtagAnalyticsService {
    private static final int MAX_DAYS = 366;
    private final HashtagAnalyticsRepository repository;
    private final Clock clock;

    public HashtagAnalyticsServiceImpl(HashtagAnalyticsRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public HashtagAnalyticsResponse getAnalytics(String range, String fromValue, String toValue) {
        DateRange selected = resolveRange(range, fromValue, toValue);
        long days = ChronoUnit.DAYS.between(selected.from(), selected.to()) + 1;
        var fromTime = selected.from().atStartOfDay();
        var toExclusive = selected.to().plusDays(1).atStartOfDay();
        var previousFrom = selected.from().minusDays(days).atStartOfDay();
        var kpi = repository.summarizeKpis(fromTime, toExclusive);
        long previousNew = repository.countNewHashtags(previousFrom, fromTime);
        boolean monthly = days > 90;

        var popular = repository.findPopular(fromTime, toExclusive).stream()
                .map(item -> new HashtagAnalyticsResponse.PopularHashtag(
                        item.id(), item.name(), item.count(), percentage(item.count(), kpi.taggedPosts())))
                .toList();
        long topPosts = popular.stream().mapToLong(HashtagAnalyticsResponse.PopularHashtag::postCount).sum();
        var growth = repository.findGrowth(previousFrom, fromTime, toExclusive).stream()
                .map(item -> new HashtagAnalyticsResponse.GrowthHashtag(item.id(), item.name(),
                        item.previousCount(), item.currentCount(), change(item.currentCount(), item.previousCount())))
                .toList();
        var recent = repository.findRecent(fromTime, toExclusive).stream()
                .map(item -> new HashtagAnalyticsResponse.RecentHashtag(item.id(), item.name(),
                        item.linkedPostCount(), item.periodPostCount(), item.createdAt(), item.latestUsedAt()))
                .toList();
        var lowUsage = repository.findLowUsage().stream()
                .map(item -> new HashtagAnalyticsResponse.LowUsageHashtag(item.id(), item.name(), item.count()))
                .toList();

        return new HashtagAnalyticsResponse(selected.from(), selected.to(), monthly ? "MONTH" : "DAY",
                new HashtagAnalyticsResponse.Kpis(kpi.totalHashtags(), kpi.usedHashtags(), kpi.newHashtags(),
                        change(kpi.newHashtags(), previousNew), kpi.taggedPosts(), kpi.totalPosts(),
                        percentage(kpi.taggedPosts(), kpi.totalPosts()), ratio(kpi.taggedPosts(), kpi.usedHashtags())),
                buildTrend(selected, monthly, repository.findTrend(fromTime, toExclusive, monthly)),
                popular, new HashtagAnalyticsResponse.Distribution(topPosts, Math.max(0, kpi.taggedPosts() - topPosts)),
                growth, recent, lowUsage);
    }

    private List<HashtagAnalyticsResponse.TrendPoint> buildTrend(
            DateRange range, boolean monthly, List<HashtagAnalyticsRepository.TrendCount> counts) {
        Map<String, HashtagAnalyticsRepository.TrendCount> indexed = new LinkedHashMap<>();
        counts.forEach(item -> indexed.put(item.period(), item));
        List<HashtagAnalyticsResponse.TrendPoint> result = new ArrayList<>();
        if (monthly) {
            for (YearMonth month = YearMonth.from(range.from()); !month.isAfter(YearMonth.from(range.to())); month = month.plusMonths(1)) {
                var item = indexed.get(month.toString());
                result.add(new HashtagAnalyticsResponse.TrendPoint(month.toString(),
                        item == null ? 0 : item.taggedPosts(), item == null ? 0 : item.totalPosts()));
            }
        } else {
            for (LocalDate day = range.from(); !day.isAfter(range.to()); day = day.plusDays(1)) {
                var item = indexed.get(day.toString());
                result.add(new HashtagAnalyticsResponse.TrendPoint(day.toString(),
                        item == null ? 0 : item.taggedPosts(), item == null ? 0 : item.totalPosts()));
            }
        }
        return List.copyOf(result);
    }

    private DateRange resolveRange(String range, String fromValue, String toValue) {
        LocalDate today = LocalDate.now(clock);
        LocalDate from;
        LocalDate to = today;
        try {
            if (fromValue != null || toValue != null) {
                if (fromValue == null || toValue == null) throw new DateTimeParseException("missing", "", 0);
                from = LocalDate.parse(fromValue);
                to = LocalDate.parse(toValue);
            } else {
                from = switch (range == null ? "30D" : range.toUpperCase()) {
                    case "7D" -> today.minusDays(6);
                    case "30D" -> today.minusDays(29);
                    case "90D" -> today.minusDays(89);
                    case "6M" -> today.minusMonths(6).plusDays(1);
                    case "1Y" -> today.minusYears(1).plusDays(1);
                    default -> throw new DateTimeParseException("range", String.valueOf(range), 0);
                };
            }
        } catch (DateTimeParseException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (from.isAfter(to) || to.isAfter(today) || days > MAX_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return new DateRange(from, to);
    }

    private BigDecimal change(long current, long previous) {
        if (previous == 0) return current == 0 ? BigDecimal.ZERO : null;
        return BigDecimal.valueOf(current - previous).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(long value, long base) {
        return base == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(base), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(long value, long base) {
        return base == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(base), 1, RoundingMode.HALF_UP);
    }

    private record DateRange(LocalDate from, LocalDate to) {}
}
