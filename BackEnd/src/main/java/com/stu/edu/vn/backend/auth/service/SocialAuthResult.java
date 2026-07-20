package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.user.enums.UserRole;

/** Kết quả phiên dùng chung sau khi provider-specific verifier đã hoàn tất. */
public record SocialAuthResult(
        String accessToken, String refreshToken, long accessTokenExpiresIn, long refreshTokenExpiresIn,
        boolean profileCompleted, String nextStep, AuthProvider provider, Long userId, UserRole role
) implements com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictResponse.SocialAuthResultView { }
