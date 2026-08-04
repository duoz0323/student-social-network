package com.stu.edu.vn.backend.search.cursor;

import java.time.LocalDateTime;

/** Cursor giữ đủ khóa sắp xếp của tìm kiếm nội dung: độ liên quan, thời gian xuất bản và postId. */
public record SearchContentCursor(String keyword, Double relevance, LocalDateTime publishedAt, Long postId) {
    public boolean isValidFor(String expectedKeyword) {
        return keyword != null && keyword.equals(expectedKeyword)
                && relevance != null && relevance >= 0
                && publishedAt != null && postId != null && postId > 0;
    }
}
