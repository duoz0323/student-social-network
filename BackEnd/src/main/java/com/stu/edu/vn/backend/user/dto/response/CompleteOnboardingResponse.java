package com.stu.edu.vn.backend.user.dto.response;

import java.time.LocalDate;

/**
 * Response sau khi hoàn tất onboarding, giúp Frontend chuyển người dùng sang Feed.
 */
public record CompleteOnboardingResponse(
        String displayName,
        String avatarUrl,
        LocalDate dateOfBirth,
        String bio,
        boolean profileCompleted,
        String nextStep
) {
}
