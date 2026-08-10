package com.stu.edu.vn.backend.user.dto.request;

import java.time.LocalDate;
import java.util.List;

/**
 * Request cập nhật hồ sơ sau onboarding, không cho cập nhật avatar qua JSON.
 */
public record UpdateUserProfileRequest(
        String displayName,
        LocalDate dateOfBirth,
        String bio,
        AcademicProfileRequest academic,
        List<Long> interestIds
) {
    /** Giữ tương thích source với các caller cũ chưa gửi Academic Profile. */
    public UpdateUserProfileRequest(String displayName, LocalDate dateOfBirth, String bio) {
        this(displayName, dateOfBirth, bio, null, null);
    }
}
