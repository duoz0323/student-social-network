package com.stu.edu.vn.backend.common.cursor;

import java.time.LocalDateTime;

/** Cursor giữ đủ khóa ORDER BY của Feed For You: score, thời gian xuất bản và postId. */
public record ForYouCursor(
        Integer version,
        LocalDateTime rankingAt,
        Integer score,
        LocalDateTime publishedAt,
        Long postId
) {
    public static final int CURRENT_VERSION = 2;

    public boolean isValid() {
        return version != null && version == CURRENT_VERSION
                && rankingAt != null
                && score != null && score >= 0
                && publishedAt != null
                && postId != null && postId > 0;
    }
}
