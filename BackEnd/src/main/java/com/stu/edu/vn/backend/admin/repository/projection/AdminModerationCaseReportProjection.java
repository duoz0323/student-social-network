package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection từng Report trong chi tiết case, bao gồm snapshot riêng của báo cáo đó. */
public interface AdminModerationCaseReportProjection {
    Long getReportId();
    String getStatus();
    String getReason();
    String getDescription();
    Long getReporterId();
    String getReporterDisplayName();
    String getReporterAvatarUrl();
    String getReporterAccountStatus();
    String getContentSnapshot();
    String getMediaSnapshot();
    LocalDateTime getCreatedAt();
    LocalDateTime getResolvedAt();
}
