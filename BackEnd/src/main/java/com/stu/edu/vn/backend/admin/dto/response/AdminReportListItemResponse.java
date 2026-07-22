package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Một báo cáo trong trang danh sách dành cho ADMIN, không chứa Entity hoặc dữ liệu nhạy cảm. */
public record AdminReportListItemResponse(
        Long reportId,
        ReportStatus status,
        ReportReason reason,
        String description,
        AdminReportUserResponse reporter,
        AdminReportPostSummaryResponse post,
        long snapshotMediaCount,
        LocalDateTime createdAt
) {
}
