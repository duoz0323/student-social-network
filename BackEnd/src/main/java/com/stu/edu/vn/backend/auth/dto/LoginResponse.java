package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.user.enums.UserRole;

/**
 * Response đăng nhập chỉ trả dữ liệu cần cho phiên và điều hướng, không trả Entity hoặc dữ liệu nhạy cảm.
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean profileCompleted,
        UserSummary user
) {

    public static final String BEARER_TOKEN_TYPE = "Bearer";

    /**
     * Thông tin người dùng tối giản để Frontend phân quyền giao diện sau đăng nhập.
     */
    public record UserSummary(
            Long id,
            UserRole role
    ) {
    }
}
