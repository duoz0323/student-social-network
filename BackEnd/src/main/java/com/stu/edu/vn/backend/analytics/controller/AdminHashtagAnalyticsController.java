package com.stu.edu.vn.backend.analytics.controller;

import com.stu.edu.vn.backend.analytics.dto.HashtagAnalyticsResponse;
import com.stu.edu.vn.backend.analytics.service.HashtagAnalyticsService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API chỉ đọc dành cho màn hình Thống kê Hashtag. */
@RestController
@RequestMapping("/api/v1/admin/analytics/hashtags")
public class AdminHashtagAnalyticsController {
    private final HashtagAnalyticsService service;

    public AdminHashtagAnalyticsController(HashtagAnalyticsService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('HASHTAG_VIEW')")
    public ResponseEntity<ApiResponse<HashtagAnalyticsResponse>> getAnalytics(
            @RequestParam(defaultValue = "30D") String range,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê hashtag thành công",
                service.getAnalytics(range, fromDate, toDate)));
    }
}
