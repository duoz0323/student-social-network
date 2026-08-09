package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ProfileReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Một lượt báo cáo thuộc vụ việc trang cá nhân, giữ rõ người gửi và lý do. */
public record AdminProfileReportReporterResponse(
        Long reportId,
        Long reporterId,
        String reporterDisplayName,
        ProfileReportReason reason,
        ReportStatus status,
        LocalDateTime createdAt
) {
}
