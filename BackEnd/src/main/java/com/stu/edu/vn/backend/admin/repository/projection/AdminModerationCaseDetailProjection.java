package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection scalar cho thông tin chung và bài viết hiện tại của case. */
public interface AdminModerationCaseDetailProjection {
    Long getCaseId();
    String getStatus();
    Long getReportCount();
    Long getDistinctReporterCount();
    LocalDateTime getFirstReportedAt();
    LocalDateTime getLatestReportedAt();
    Long getPostId();
    String getPostCurrentStatus();
    String getPostCurrentContent();
    Long getAuthorId();
    String getAuthorDisplayName();
    String getAuthorAvatarUrl();
    String getAuthorAccountStatus();
    LocalDateTime getHiddenAt();
    String getHiddenReason();
    LocalDateTime getDeletedAt();
    Long getResolvedByAdminId();
    String getResolvedByDisplayName();
    LocalDateTime getResolvedAt();
    String getResolutionNote();
}
