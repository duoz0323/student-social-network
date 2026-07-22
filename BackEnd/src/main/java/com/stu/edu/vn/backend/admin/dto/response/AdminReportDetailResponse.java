package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Chi tiết báo cáo dành cho ADMIN, phân biệt dữ liệu hiện tại và snapshot bằng chứng. */
public record AdminReportDetailResponse(
        Long reportId,
        ReportStatus status,
        ReportReason reason,
        String description,
        AdminReportUserResponse reporter,
        AdminReportedPostResponse reportedPost,
        AdminReportEvidenceResponse evidence,
        AdminReportResolutionResponse resolution,
        LocalDateTime createdAt
) {
}
