package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Scalar projection duy nhất cho màn hình chi tiết report quản trị. */
public interface AdminReportDetailProjection {
    Long getReportId();
    String getStatus();
    String getReason();
    String getDescription();
    Long getReporterId();
    String getReporterDisplayName();
    String getReporterAvatarUrl();
    String getReporterAccountStatus();
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
    String getContentSnapshot();
    String getMediaSnapshot();
    Long getResolvedByAdminId();
    String getResolvedByDisplayName();
    LocalDateTime getResolvedAt();
    String getResolutionNote();
    LocalDateTime getCreatedAt();
}
