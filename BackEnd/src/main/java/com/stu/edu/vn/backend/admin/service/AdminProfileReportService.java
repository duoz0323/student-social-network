package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.request.AdminProfileReportResolutionRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportStatusResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.report.enums.ReportStatus;

public interface AdminProfileReportService {
    PageResponse<AdminProfileReportListItemResponse> getReports(
            ReportStatus status, String keyword, int page, int size);

    AdminProfileReportDetailResponse getDetail(Long caseId);

    AdminProfileReportStatusResponse reject(Long caseId, AdminProfileReportResolutionRequest request);

    AdminProfileReportStatusResponse resolve(Long caseId, AdminProfileReportResolutionRequest request);
}
