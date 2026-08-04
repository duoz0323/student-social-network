package com.stu.edu.vn.backend.analytics.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Chuỗi chỉ số theo tháng và hai mốc peak phục vụ riêng module Analytics của Admin.
 */
public record MonthlyUserEngagementResponse(
        YearMonth fromMonth,
        YearMonth toMonth,
        int inactiveDays,
        String comparisonOperator,
        YearMonth peakReturningMonth,
        long peakReturningUserCount,
        YearMonth peakReturnRateMonth,
        BigDecimal peakReturnRate,
        List<MonthlyUserEngagementItemResponse> items
) {
}
