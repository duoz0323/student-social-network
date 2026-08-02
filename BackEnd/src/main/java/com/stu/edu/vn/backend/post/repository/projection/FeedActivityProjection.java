package com.stu.edu.vn.backend.post.repository.projection;

import java.time.LocalDateTime;

/** Projection khóa timeline; dữ liệu PostCard được batch-load sau để tránh N+1. */
public interface FeedActivityProjection {
    Long getPostId();

    LocalDateTime getActivityAt();

    Integer getItemRank();

    Long getActorId();

    LocalDateTime getRepostedAt();
}
