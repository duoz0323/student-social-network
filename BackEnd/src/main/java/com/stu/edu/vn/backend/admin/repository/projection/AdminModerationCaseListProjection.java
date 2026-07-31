package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection trang Moderation Case; không tải collection Report. */
public interface AdminModerationCaseListProjection {
    Long getCaseId();
    String getStatus();
    Long getPostId();
    String getPostContentPreview();
    Long getPostAuthorId();
    String getPostAuthorDisplayName();
    Long getReportCount();
    Long getDistinctReporterCount();
    Long getResolvedBy();
    String getResolvedByDisplayName();
    LocalDateTime getFirstReportedAt();
    LocalDateTime getLatestReportedAt();
    LocalDateTime getResolvedAt();
}
