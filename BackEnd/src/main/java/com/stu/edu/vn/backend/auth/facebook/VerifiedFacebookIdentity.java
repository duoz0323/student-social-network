package com.stu.edu.vn.backend.auth.facebook;

import java.time.Instant;

/** Danh tính nội bộ chỉ được tạo sau khi debug_token và /me cùng thành công. */
public record VerifiedFacebookIdentity(
        String providerUserId, String email, String displayName, String avatarUrl,
        String appId, Instant tokenExpiresAt
) { }
