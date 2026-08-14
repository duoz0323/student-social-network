package com.stu.edu.vn.backend.feed.repository.projection;

import java.time.LocalDateTime;

/** Projection nội bộ giữ khóa xếp hạng; score không đi ra public Feed API. */
public interface PersonalizedPostRankProjection {
    Long getPostId();

    Integer getScore();

    LocalDateTime getPublishedAt();
}
