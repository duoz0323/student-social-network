package com.stu.edu.vn.backend.analytics.controller;

import com.stu.edu.vn.backend.analytics.dto.DashboardUserEngagementResponse;
import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementItemResponse;
import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementResponse;
import com.stu.edu.vn.backend.analytics.service.UserEngagementAnalyticsService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API analytics độc lập dành cho ADMIN, không thuộc API Dashboard.
 */
@RestController
@RequestMapping("/api/v1/admin/analytics/user-engagement")
public class  AdminUserEngagementAnalyticsController {

    private final UserEngagementAnalyticsService analyticsService;

    public AdminUserEngagementAnalyticsController(UserEngagementAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Cấp dữ liệu Dashboard cho biểu đồ tương tác và bảng USER nổi bật, không trả lịch sử hoạt động chi tiết.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('DASHBOARD_BASIC_VIEW')")
    public ResponseEntity<ApiResponse<DashboardUserEngagementResponse>> getDashboard(
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thống kê tương tác Dashboard thành công",
                analyticsService.getDashboard(days)
        ));
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAuthority('USER_ANALYTICS_VIEW')")
    public ResponseEntity<ApiResponse<MonthlyUserEngagementResponse>> getMonthly(
            @RequestParam String fromMonth,
            @RequestParam String toMonth,
            @RequestParam(defaultValue = "15") int inactiveDays
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thống kê hoạt động người dùng theo tháng thành công",
                analyticsService.getMonthly(fromMonth, toMonth, inactiveDays)
        ));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('USER_ANALYTICS_VIEW')")
    public ResponseEntity<ApiResponse<MonthlyUserEngagementItemResponse>> getSummary(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "15") int inactiveDays
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy tổng hợp hoạt động người dùng thành công",
                analyticsService.getSummary(month, inactiveDays)
        ));
    }
}
