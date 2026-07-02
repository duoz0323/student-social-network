package com.stu.edu.vn.backend.common.api;

/**
 * Mô tả lỗi theo từng trường để Frontend hiển thị đúng vị trí nhập liệu.
 */
public record FieldErrorDetail(
        String field,
        String message
) {
}
