package com.stu.edu.vn.backend.follow.repository;

import java.time.LocalDateTime;

/**
 * Projection giới hạn cột truy vấn danh sách Follow và giữ Boolean nullable từ kết quả MySQL.
 */
public interface FollowUserProjection {

    Long getUserId();

    String getDisplayName();

    String getAvatarUrl();

    String getBio();

    LocalDateTime getFollowedAt();

    Boolean getFollowedByCurrentUser();
}
