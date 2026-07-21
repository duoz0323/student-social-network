package com.stu.edu.vn.backend.user.dto.response;

import java.time.LocalDate;

/**
 * Response trạng thái onboarding của người dùng hiện tại, không trả dữ liệu xác thực nhạy cảm.
 */
public record OnboardingStatusResponse(
        String displayName,
        String avatarUrl,
        LocalDate dateOfBirth,
        String bio,
        boolean profileCompleted,
        String nextStep
) {
}
