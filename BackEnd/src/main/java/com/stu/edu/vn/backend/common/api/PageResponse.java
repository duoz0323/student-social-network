package com.stu.edu.vn.backend.common.api;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Phản hồi phân trang ổn định, không để contract API phụ thuộc cấu trúc serialization của Spring Page.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> source) {
        return new PageResponse<>(
                source.getContent(), source.getNumber(), source.getSize(), source.getTotalElements(),
                source.getTotalPages(), source.isFirst(), source.isLast()
        );
    }
}
