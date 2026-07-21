package com.stu.edu.vn.backend.user.dto.response;

import java.time.LocalDate;

/**
 * Response hồ sơ cá nhân sau onboarding; không chứa email hoặc token xác thực.
 */
public record UserProfileResponse(
        String displayName,
        String avatarUrl,
        LocalDate dateOfBirth,
        String bio,
        boolean profileCompleted
) {
}
