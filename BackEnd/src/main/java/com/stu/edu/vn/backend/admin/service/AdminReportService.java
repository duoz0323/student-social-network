package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.request.AdminRejectReportRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminResolveReportRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;

/** Use case đọc và xử lý báo cáo dành cho ADMIN. */
public interface AdminReportService {
    PageResponse<AdminReportListItemResponse> getReports(
            ReportStatus status,
            ReportReason reason,
            Long postId,
            Long reporterId,
            Long authorId,
            String keyword,
            int page,
            int size);

    AdminReportDetailResponse getReportDetail(Long reportId);

    AdminReportStatusResponse rejectReport(Long reportId, AdminRejectReportRequest request);

    AdminReportStatusResponse resolveReport(Long reportId, AdminResolveReportRequest request);
}
