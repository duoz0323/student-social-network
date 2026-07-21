package com.stu.edu.vn.backend.user.dto.request;

import java.time.LocalDate;

/**
 * Request hoàn tất hồ sơ ban đầu; không nhận userId hay avatar để tránh Client cập nhật sai chủ sở hữu.
 */
public record CompleteOnboardingRequest(
        String displayName,
        LocalDate dateOfBirth,
        String bio
) {
}
