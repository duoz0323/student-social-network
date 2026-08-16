package com.stu.edu.vn.backend.analytics.controller;

import com.stu.edu.vn.backend.analytics.dto.PostAnalyticsResponse;
import com.stu.edu.vn.backend.analytics.service.PostAnalyticsService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API chỉ đọc dành cho màn hình Thống kê bài viết. */
@RestController
@RequestMapping("/api/v1/admin/analytics/posts")
public class AdminPostAnalyticsController {
    private final PostAnalyticsService service;
    public AdminPostAnalyticsController(PostAnalyticsService service){this.service=service;}

    @GetMapping
    @PreAuthorize("hasAuthority('POST_VIEW')")
    public ResponseEntity<ApiResponse<PostAnalyticsResponse>> getAnalytics(
            @RequestParam(defaultValue="30D") String range,
            @RequestParam(required=false) String fromDate,
            @RequestParam(required=false) String toDate){
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê bài viết thành công",service.getAnalytics(range,fromDate,toDate)));
    }
}
