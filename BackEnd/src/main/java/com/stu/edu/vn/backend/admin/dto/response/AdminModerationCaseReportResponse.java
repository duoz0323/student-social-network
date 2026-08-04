package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Report độc lập trong chi tiết case, giữ nguyên reporter, reason, description và snapshot riêng. */
public record AdminModerationCaseReportResponse(
        Long reportId,
        AdminReportUserResponse reporter,
        ReportReason reason,
        String description,
        ReportStatus status,
        AdminReportEvidenceResponse evidence,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
