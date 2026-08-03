package com.stu.edu.vn.backend.analytics.service;

import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementItemResponse;
import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementResponse;
import com.stu.edu.vn.backend.analytics.repository.MonthlyUserEngagementCounts;
import com.stu.edu.vn.backend.analytics.repository.UserEngagementAnalyticsRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service kiểm soát khoảng tháng, evaluationDate và các công thức tỷ lệ thống nhất.
 */
@Service
@Transactional(readOnly = true)
public class UserEngagementAnalyticsServiceImpl implements UserEngagementAnalyticsService {

    static final int MAX_MONTHS = 24;
    private final UserEngagementAnalyticsRepository repository;
    private final Clock clock;

    public UserEngagementAnalyticsServiceImpl(UserEngagementAnalyticsRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public MonthlyUserEngagementResponse getMonthly(String fromMonthValue, String toMonthValue, int inactiveDays) {
        YearMonth fromMonth = parseRequiredMonth(fromMonthValue);
        YearMonth toMonth = parseRequiredMonth(toMonthValue);
        validateRange(fromMonth, toMonth, inactiveDays);

        List<MonthlyUserEngagementItemResponse> items = new ArrayList<>();
        for (YearMonth month = fromMonth; !month.isAfter(toMonth); month = month.plusMonths(1)) {
            items.add(buildItem(month, inactiveDays));
        }

        MonthlyUserEngagementItemResponse peakReturning = items.getFirst();
        MonthlyUserEngagementItemResponse peakRate = null;
        for (MonthlyUserEngagementItemResponse item : items) {
            if (item.returningUserCount() > peakReturning.returningUserCount()) {
                peakReturning = item;
            }
            if (item.returnRate() != null
                    && (peakRate == null || item.returnRate().compareTo(peakRate.returnRate()) > 0)) {
                peakRate = item;
            }
        }

        return new MonthlyUserEngagementResponse(
                fromMonth,
                toMonth,
                inactiveDays,
                "GREATER_THAN",
                peakReturning.month(),
                peakReturning.returningUserCount(),
                peakRate == null ? null : peakRate.month(),
                peakRate == null ? null : peakRate.returnRate(),
                List.copyOf(items)
        );
    }

    @Override
    public MonthlyUserEngagementItemResponse getSummary(String monthValue, int inactiveDays) {
        YearMonth currentMonth = YearMonth.from(LocalDate.now(clock));
        YearMonth month = monthValue == null || monthValue.isBlank() ? currentMonth : parseRequiredMonth(monthValue);
        validateRange(month, month, inactiveDays);
        return buildItem(month, inactiveDays);
    }

    private MonthlyUserEngagementItemResponse buildItem(YearMonth month, int inactiveDays) {
        LocalDate today = LocalDate.now(clock);
        LocalDate evaluationDate = month.equals(YearMonth.from(today)) ? today : month.atEndOfMonth();
        LocalDateTime monthStart = month.atDay(1).atStartOfDay();
        LocalDateTime evaluationEndExclusive = evaluationDate.plusDays(1).atStartOfDay();
        MonthlyUserEngagementCounts counts = repository.summarizeMonth(
                monthStart, evaluationEndExclusive, inactiveDays);

        long active = counts.newActiveUserCount()
                + counts.regularActiveUserCount()
                + counts.returningUserCount();
        long inactive = counts.recentlyInactiveUserCount()
                + counts.eligibleInactiveNotReturnedUserCount()
                + counts.neverActiveUserCount();
        long eligibleInactive = counts.returningEligibleUserCount()
                + counts.eligibleInactiveNotReturnedUserCount();

        return new MonthlyUserEngagementItemResponse(
                month,
                evaluationDate,
                counts.eligibleSystemUserCount(),
                active,
                percentage(active, counts.eligibleSystemUserCount()),
                counts.newActiveUserCount(),
                counts.regularActiveUserCount(),
                percentage(counts.regularActiveUserCount(), active),
                counts.returningUserCount(),
                counts.recentlyInactiveUserCount(),
                eligibleInactive,
                counts.returningEligibleUserCount(),
                counts.eligibleInactiveNotReturnedUserCount(),
                percentage(counts.returningEligibleUserCount(), eligibleInactive),
                counts.neverActiveUserCount(),
                percentage(counts.neverActiveUserCount(), counts.eligibleSystemUserCount()),
                inactive
        );
    }

    private void validateRange(YearMonth fromMonth, YearMonth toMonth, int inactiveDays) {
        if (inactiveDays < 1 || inactiveDays > 365) {
            throw new BusinessException(ErrorCode.ANALYTICS_INACTIVE_DAYS_INVALID);
        }
        if (fromMonth.isAfter(toMonth)) {
            throw new BusinessException(ErrorCode.ANALYTICS_MONTH_RANGE_INVALID);
        }
        if (toMonth.isAfter(YearMonth.from(LocalDate.now(clock)))) {
            throw new BusinessException(ErrorCode.ANALYTICS_FUTURE_MONTH);
        }
        if (ChronoUnit.MONTHS.between(fromMonth, toMonth) + 1 > MAX_MONTHS) {
            throw new BusinessException(ErrorCode.ANALYTICS_MONTH_RANGE_TOO_LARGE);
        }
    }

    private YearMonth parseRequiredMonth(String value) {
        if (value == null || !value.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new BusinessException(ErrorCode.ANALYTICS_MONTH_INVALID);
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(ErrorCode.ANALYTICS_MONTH_INVALID);
        }
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return null;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
