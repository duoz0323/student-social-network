package com.stu.edu.vn.backend.report.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.report.dto.request.CreateReportRequest;
import com.stu.edu.vn.backend.report.dto.response.CreateReportResponse;
import com.stu.edu.vn.backend.report.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tiếp nhận duy nhất thao tác USER báo cáo bài viết; không chứa API xử lý báo cáo của Admin.
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateReportResponse>> createPostReport(
            @PathVariable Long postId,
            @Valid @RequestBody CreateReportRequest request
    ) {
        // Service tự lấy reporterId từ JWT/SecurityContext và tự tạo snapshot từ database.
        CreateReportResponse response = reportService.createPostReport(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi báo cáo bài viết thành công", response));
    }
}
