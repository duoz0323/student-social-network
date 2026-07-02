package com.stu.edu.vn.backend.auth.support;

/**
 * Kết quả chuẩn hóa identifier để Service lưu đúng cột email hoặc phone_number.
 */
public record NormalizedIdentifier(
        IdentifierType type,
        String value
) {
}
