package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import java.time.LocalDateTime;
import java.util.List;

/** Contract chi tiết case gồm bài hiện tại, toàn bộ bằng chứng và quyết định cuối. */
public record AdminModerationCaseDetailResponse(
        Long caseId,
        ModerationCaseStatus status,
        long reportCount,
        long distinctReporterCount,
        LocalDateTime firstReportedAt,
        LocalDateTime latestReportedAt,
        AdminReportedPostResponse reportedPost,
        List<ModerationReasonCountResponse> reasons,
        List<AdminModerationCaseReportResponse> reports,
        AdminReportResolutionResponse resolution,
        List<AdminModerationCaseActionResponse> adminActions
) {
}
