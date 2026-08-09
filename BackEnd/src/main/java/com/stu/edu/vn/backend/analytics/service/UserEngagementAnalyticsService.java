package com.stu.edu.vn.backend.analytics.service;

import com.stu.edu.vn.backend.analytics.dto.DashboardUserEngagementResponse;
import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementItemResponse;
import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementResponse;

/**
 * Contract thống kê engagement chỉ dành cho Admin.
 */
public interface UserEngagementAnalyticsService {
    DashboardUserEngagementResponse getDashboard(int days);

    MonthlyUserEngagementResponse getMonthly(String fromMonth, String toMonth, int inactiveDays);

    MonthlyUserEngagementItemResponse getSummary(String month, int inactiveDays);
}
