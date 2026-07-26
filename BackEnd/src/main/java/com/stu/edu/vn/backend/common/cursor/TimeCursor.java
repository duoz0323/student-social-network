package com.stu.edu.vn.backend.common.cursor;

import java.time.LocalDateTime;

/** Cursor cho danh sách sắp xếp giảm dần theo thời gian và postId. */
public record TimeCursor(LocalDateTime createdAt, Long postId) {
    public boolean isValid() {
        return createdAt != null && postId != null && postId > 0;
    }
}
