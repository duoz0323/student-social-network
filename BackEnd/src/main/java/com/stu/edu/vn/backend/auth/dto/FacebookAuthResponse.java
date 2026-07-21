package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.user.enums.UserRole;

/** Response chỉ chứa token của hệ thống và thông tin điều hướng tối thiểu. */
public record FacebookAuthResponse(
        String accessToken, String refreshToken, String tokenType,
        long accessTokenExpiresIn, long refreshTokenExpiresIn,
        boolean profileCompleted, String nextStep, AuthProvider authenticationMethod, UserSummary user
) {
    public record UserSummary(Long id, UserRole role) { }
}
