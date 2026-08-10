package com.stu.edu.vn.backend.user.dto.response;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.InterestResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * Dữ liệu hiển thị trang hồ sơ; không chứa email hoặc dữ liệu xác thực nhạy cảm.
 */
public record UserProfileViewResponse(
        Long userId,
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
        long followerCount,
        long followingCount,
        boolean followedByCurrentUser,
        boolean blockedByMe,
        boolean restrictedByMe
) {
}
