package com.stu.edu.vn.backend.admin.dto.response;

import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import java.time.LocalDateTime;
import java.util.List;

/** Một dòng danh sách quản trị đại diện cho đúng một Moderation Case. */
public record AdminModerationCaseListItemResponse(
        Long caseId,
        Long postId,
        String postContentPreview,
        Long postAuthorId,
        String postAuthorDisplayName,
        long reportCount,
        long distinctReporterCount,
        List<ModerationReasonCountResponse> reasons,
        ModerationCaseStatus status,
        LocalDateTime firstReportedAt,
        LocalDateTime latestReportedAt,
        Long resolvedBy,
        String resolvedByDisplayName,
        LocalDateTime resolvedAt
) {
}
