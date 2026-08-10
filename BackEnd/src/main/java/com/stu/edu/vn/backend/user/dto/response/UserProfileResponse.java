package com.stu.edu.vn.backend.user.dto.response;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.InterestResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * Response hồ sơ cá nhân sau onboarding; không chứa email hoặc token xác thực.
 */
public record UserProfileResponse(
        String username,
        String displayName,
        String avatarUrl,
        LocalDate dateOfBirth,
        String bio,
        SchoolResponse school,
        AcademicItemResponse faculty,
        AcademicItemResponse major,
        Integer entryYear,
        List<InterestResponse> interests,
        boolean profileCompleted
) {
}
