package com.stu.edu.vn.backend.common.api;

import java.util.List;

/**
 * Response dùng chung cho danh sách cuộn vô hạn, không trả tổng số và không phụ thuộc offset.
 */
public record CursorPageResponse<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext
) {
}
