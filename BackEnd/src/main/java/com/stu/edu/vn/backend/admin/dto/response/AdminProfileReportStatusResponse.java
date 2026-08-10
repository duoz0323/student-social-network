package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ReportStatus;
import java.time.LocalDateTime;

/** Trạng thái mới sau khi Admin kết luận toàn bộ vụ việc báo cáo hồ sơ. */
public record AdminProfileReportStatusResponse(
        Long caseId,
        ReportStatus status,
        Long resolvedById,
        LocalDateTime resolvedAt,
        String resolutionNote,
        boolean accountBlocked
) {
}
