package com.stu.edu.vn.backend.search.cursor;

import java.time.LocalDateTime;

/** Cursor hashtag gắn với từ khóa để không thể tái sử dụng nhầm giữa hai truy vấn Search. */
public record SearchHashtagCursor(String hashtag, LocalDateTime publishedAt, Long postId) {
    public boolean isValidFor(String expectedHashtag) {
        return hashtag != null && hashtag.equals(expectedHashtag)
                && publishedAt != null && postId != null && postId > 0;
    }
}
