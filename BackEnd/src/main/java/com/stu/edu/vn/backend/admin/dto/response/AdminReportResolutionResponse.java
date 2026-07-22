package com.stu.edu.vn.backend.admin.dto.response;

import java.time.LocalDateTime;

/** Kết quả xử lý đã có trong schema; các trường để null khi báo cáo còn PENDING. */
public record AdminReportResolutionResponse(
        AdminReportResolvedByResponse resolvedBy,
        LocalDateTime resolvedAt,
        String resolutionNote
) {
}
