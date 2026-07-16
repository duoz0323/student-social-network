package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Response rút gọn sau mutation Report, không trả snapshot hoặc toàn bộ Post. */
public record AdminReportStatusResponse(
        Long reportId,
        ReportStatus status,
        LocalDateTime resolvedAt,
        String resolutionNote,
        AdminReportResolvedByResponse resolvedBy,
        AdminReportStatusPostResponse post
) {
}
