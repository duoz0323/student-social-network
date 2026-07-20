package com.stu.edu.vn.backend.auth.dto;

/**
 * Response refresh trả cặp token mới để Client thay thế Refresh Token cũ ngay sau mỗi lần sử dụng.
 */
public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean profileCompleted
) {

    public static final String BEARER_TOKEN_TYPE = "Bearer";
}
