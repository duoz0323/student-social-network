package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.List;

/** Chi tiết báo cáo hồ sơ; dữ liệu hiện tại của USER được đọc qua API quản lý người dùng. */
public record AdminProfileReportDetailResponse(
        Long caseId,
        ReportStatus status,
        int reportCount,
        Long reportedUserId,
        AdminProfileReportSnapshotResponse snapshot,
        List<AdminProfileReportReporterResponse> reports,
        LocalDateTime createdAt,
        LocalDateTime latestReportedAt,
        Long resolvedById,
        LocalDateTime resolvedAt,
        String resolutionNote
) {
}
