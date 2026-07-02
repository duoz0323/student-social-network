package com.stu.edu.vn.backend.auth.dto;

/**
 * Response refresh chỉ cấp Access Token mới, không rotate hoặc trả lại Refresh Token trong giai đoạn đầu.
 */
public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        long accessTokenExpiresIn,
        boolean profileCompleted
) {

    public static final String BEARER_TOKEN_TYPE = "Bearer";
}
