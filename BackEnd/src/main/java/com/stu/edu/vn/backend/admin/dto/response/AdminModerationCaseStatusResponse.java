package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import java.time.LocalDateTime;

/** Response gọn sau khi Admin đưa ra quyết định cuối cùng. */
public record AdminModerationCaseStatusResponse(
        Long caseId,
        ModerationCaseStatus status,
        LocalDateTime resolvedAt,
        String resolutionNote,
        AdminReportResolvedByResponse resolvedBy,
        AdminReportStatusPostResponse post,
        long authorViolationCount,
        boolean accountBlocked
) {
}
