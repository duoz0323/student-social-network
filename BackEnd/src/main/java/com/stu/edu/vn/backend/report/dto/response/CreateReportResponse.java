package com.stu.edu.vn.backend.report.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/**
 * Response tối giản của API người dùng, không làm lộ reporter, snapshot hoặc dữ liệu xử lý nội bộ.
 */
public record CreateReportResponse(
        Long reportId,
        Long postId,
        ReportReason reason,
        ReportStatus status,
        LocalDateTime createdAt
) {
}
