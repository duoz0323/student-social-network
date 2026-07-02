package com.stu.edu.vn.backend.user.dto.request;

import java.time.LocalDate;

/**
 * Request cập nhật hồ sơ sau onboarding, không cho cập nhật avatar qua JSON.
 */
public record UpdateUserProfileRequest(
        String displayName,
        LocalDate dateOfBirth,
        String bio
) {
}
