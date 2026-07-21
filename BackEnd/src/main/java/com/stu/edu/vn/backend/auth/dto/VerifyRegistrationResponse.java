package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.user.enums.UserRole;

/** Phiên đăng nhập được cấp sau khi OTP hợp lệ và toàn bộ transaction đã hoàn tất. */
public record VerifyRegistrationResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean profileCompleted,
        String nextStep,
        UserSummary user
) {

    public static final String BEARER_TOKEN_TYPE = "Bearer";
    public static final String NEXT_STEP_ONBOARDING = "ONBOARDING";

    public record UserSummary(Long id, UserRole role) {
    }
}
