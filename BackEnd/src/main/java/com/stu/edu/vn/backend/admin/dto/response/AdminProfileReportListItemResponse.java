package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Một vụ việc hồ sơ đã gom tất cả lượt báo cáo của nhiều người dùng. */
public record AdminProfileReportListItemResponse(
        Long caseId,
        ReportStatus status,
        int reportCount,
        Long reportedUserId,
        String reportedDisplayName,
        String reportedAvatarUrl,
        LocalDateTime createdAt,
        LocalDateTime latestReportedAt
) {
}
