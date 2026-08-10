package com.stu.edu.vn.backend.analytics.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Dữ liệu analytics gọn cho Dashboard: chuỗi tương tác theo ngày và người dùng nổi bật hôm nay.
 */
public record DashboardUserEngagementResponse(
        LocalDate fromDate,
        LocalDate toDate,
        List<DailyInteractionResponse> dailyInteractions,
        List<FeaturedUserResponse> featuredUsers
) {
}
