package com.stu.edu.vn.backend.analytics.service;

import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementItemResponse;
import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementResponse;

/**
 * Contract thống kê engagement chỉ dành cho Admin.
 */
public interface UserEngagementAnalyticsService {
    MonthlyUserEngagementResponse getMonthly(String fromMonth, String toMonth, int inactiveDays);

    MonthlyUserEngagementItemResponse getSummary(String month, int inactiveDays);
}
