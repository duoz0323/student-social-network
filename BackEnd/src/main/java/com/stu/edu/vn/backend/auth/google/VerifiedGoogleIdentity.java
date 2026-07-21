package com.stu.edu.vn.backend.auth.google;

/** Danh tính nội bộ chỉ được tạo sau khi Google ID Token đã được xác minh đầy đủ. */
public record VerifiedGoogleIdentity(
        String subject,
        String email,
        boolean emailVerified,
        String displayName,
        String avatarUrl,
        String issuer
) {
}
