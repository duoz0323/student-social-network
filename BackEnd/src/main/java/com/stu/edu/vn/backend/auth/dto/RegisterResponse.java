package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;

/**
 * Response đăng ký trả phiên đăng nhập nhưng vẫn yêu cầu Frontend đi đến onboarding.
 */
public record RegisterResponse(
        Long userId,
        UserRole role,
        UserStatus status,
        boolean profileCompleted,
        String nextStep,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn
) {

    public static final String ONBOARDING_PROFILE = "ONBOARDING_PROFILE";
}
