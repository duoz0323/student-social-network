package com.stu.edu.vn.backend.report.service;

import com.stu.edu.vn.backend.report.dto.request.CreateReportRequest;
import com.stu.edu.vn.backend.report.dto.response.CreateReportResponse;

/**
 * Service nghiệp vụ tạo báo cáo bài viết dành riêng cho người dùng.
 */
public interface ReportService {

    CreateReportResponse createPostReport(Long postId, CreateReportRequest request);
}
