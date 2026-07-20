package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.user.enums.UserRole;

/** Response hợp nhất cho nhánh tiếp tục OTP hoặc phiên đăng nhập social thành công. */
public record ResolveSocialConflictResponse(
        boolean resolved,
        String accessToken,
        String refreshToken,
        String tokenType,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn,
        Boolean profileCompleted,
        String nextStep,
        AuthProvider authenticationMethod,
        UserSummary user
) {
    public static ResolveSocialConflictResponse continueOtp() {
        return new ResolveSocialConflictResponse(true, null, null, null, null, null, null,
                "VERIFY_OTP", null, null);
    }

    public static ResolveSocialConflictResponse session(SocialAuthResultView result) {
        return new ResolveSocialConflictResponse(true, result.accessToken(), result.refreshToken(), "Bearer",
                result.accessTokenExpiresIn(), result.refreshTokenExpiresIn(), result.profileCompleted(),
                result.nextStep(), result.provider(), new UserSummary(result.userId(), result.role()));
    }

    /** View nhỏ để DTO không phụ thuộc trực tiếp vào lớp service. */
    public interface SocialAuthResultView {
        String accessToken();
        String refreshToken();
        long accessTokenExpiresIn();
        long refreshTokenExpiresIn();
        boolean profileCompleted();
        String nextStep();
        AuthProvider provider();
        Long userId();
        UserRole role();
    }

    public record UserSummary(Long id, UserRole role) { }
}
