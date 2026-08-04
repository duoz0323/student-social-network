package com.stu.edu.vn.backend.feed.cursor;

import java.time.LocalDateTime;

/** Cursor giữ toàn bộ khóa sắp xếp của timeline gồm bài gốc và Repost. */
public record FollowingActivityCursor(
        LocalDateTime activityAt,
        Integer itemRank,
        Long actorId,
        Long postId
) {
    public boolean isValid() {
        return activityAt != null
                && itemRank != null && itemRank >= 0 && itemRank <= 1
                && actorId != null && actorId > 0
                && postId != null && postId > 0;
    }
}
