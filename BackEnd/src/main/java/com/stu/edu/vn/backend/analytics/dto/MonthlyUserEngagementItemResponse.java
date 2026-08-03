package com.stu.edu.vn.backend.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Chỉ số phân loại và tỷ lệ engagement của một tháng tại ngày đánh giá.
 */
public record MonthlyUserEngagementItemResponse(
        YearMonth month,
        LocalDate evaluationDate,
        long eligibleSystemUserCount,
        long activeUserCount,
        BigDecimal activeUserRate,
        long newActiveUserCount,
        long regularActiveUserCount,
        BigDecimal regularActiveRate,
        long returningUserCount,
        long recentlyInactiveUserCount,
        long eligibleInactiveUserCount,
        long returningEligibleUserCount,
        long eligibleInactiveNotReturnedUserCount,
        BigDecimal returnRate,
        long neverActiveUserCount,
        BigDecimal neverActiveRate,
        long inactiveUserCount
) {
}
