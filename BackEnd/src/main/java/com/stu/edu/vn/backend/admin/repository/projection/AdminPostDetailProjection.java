package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection header của chi tiết bài viết và các bộ đếm quản trị. */
public interface AdminPostDetailProjection {
    Long getPostId();
    String getContent();
    String getStatus();
    Long getAuthorId();
    String getAuthorDisplayName();
    String getAuthorAvatarUrl();
    String getAuthorEmail();
    String getAuthorPhoneNumber();
    String getAuthorAccountStatus();
    Integer getLikeCount();
    Integer getCommentCount();
    Long getPendingReportCount();
    Long getTotalReportCount();
    LocalDateTime getHiddenAt();
    String getHiddenReason();
    Long getHiddenByAdminId();
    String getHiddenByDisplayName();
    LocalDateTime getDeletedAt();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
