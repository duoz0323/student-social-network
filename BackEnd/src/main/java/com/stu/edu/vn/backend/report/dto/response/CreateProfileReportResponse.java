package com.stu.edu.vn.backend.report.dto.response;

import com.stu.edu.vn.backend.report.enums.ProfileReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Kết quả tối thiểu sau khi tạo báo cáo hồ sơ. */
public record CreateProfileReportResponse(
        Long reportId,
        Long reportedUserId,
        ProfileReportReason reason,
        ReportStatus status,
        LocalDateTime createdAt
) {
}
