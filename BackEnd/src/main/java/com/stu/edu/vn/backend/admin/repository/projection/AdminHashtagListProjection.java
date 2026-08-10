package com.stu.edu.vn.backend.admin.repository.projection;

import java.time.LocalDateTime;

/** Projection tránh tải Entity và quan hệ khi ADMIN chỉ cần xem bảng hashtag. */
public interface AdminHashtagListProjection {
    Long getHashtagId();

    String getName();

    Integer getPostCount();

    LocalDateTime getCreatedAt();

    LocalDateTime getLatestUsedAt();
}
