package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Scalar projection cho danh sách report để tránh materialize Entity và N+1. */
public interface AdminReportListProjection {
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
    String getContentPreview();
    Long getAuthorId();
    String getAuthorDisplayName();
    String getAuthorAccountStatus();
    Long getSnapshotMediaCount();
    LocalDateTime getCreatedAt();
}
