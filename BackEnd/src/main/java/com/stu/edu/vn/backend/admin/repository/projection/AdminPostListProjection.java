package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection phẳng cho danh sách để không khởi tạo graph Entity. */
public interface AdminPostListProjection {
    Long getPostId();
    String getContentPreview();
    String getStatus();
    Long getAuthorId();
    String getAuthorDisplayName();
    String getAuthorAvatarUrl();
    String getAuthorAccountStatus();
    String getThumbnailUrl();
    Long getMediaCount();
    Integer getLikeCount();
    Integer getCommentCount();
    Long getPendingReportCount();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
