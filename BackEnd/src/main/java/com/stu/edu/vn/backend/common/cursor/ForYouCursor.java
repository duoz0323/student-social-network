package com.stu.edu.vn.backend.common.cursor;

import java.time.LocalDateTime;

/** Cursor giữ đủ khóa ORDER BY của Feed For You: score, thời gian xuất bản và postId. */
public record ForYouCursor(Integer score, LocalDateTime createdAt, Long postId) {
    public boolean isValid() {
        return score != null && score >= 0 && createdAt != null && postId != null && postId > 0;
    }
}
