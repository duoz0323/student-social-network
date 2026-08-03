package com.stu.edu.vn.backend.analytics.repository;

/**
 * Kết quả đếm sáu nhóm loại trừ lẫn nhau của một tháng từ MySQL.
 */
public record MonthlyUserEngagementCounts(
        long eligibleSystemUserCount,
        long newActiveUserCount,
        long regularActiveUserCount,
        long returningUserCount,
        long recentlyInactiveUserCount,
        long eligibleInactiveNotReturnedUserCount,
        long neverActiveUserCount,
        long returningEligibleUserCount
) {
}
