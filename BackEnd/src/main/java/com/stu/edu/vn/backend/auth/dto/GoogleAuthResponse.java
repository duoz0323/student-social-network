package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.user.enums.UserRole;

/** Session response Google Auth chỉ chứa JWT hệ thống và dữ liệu điều hướng tối thiểu. */
public record GoogleAuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean profileCompleted,
        String nextStep,
        AuthProvider authenticationMethod,
        UserSummary user
) {
    public static final String BEARER_TOKEN_TYPE = "Bearer";
    public static final String NEXT_STEP_COMPLETE_PROFILE = "COMPLETE_PROFILE";
    public static final String NEXT_STEP_HOME = "HOME";

    public record UserSummary(Long id, UserRole role) { }
}
