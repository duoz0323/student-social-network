package com.stu.edu.vn.backend.recommendation.dto.response;

import com.stu.edu.vn.backend.recommendation.enums.StudentMatchReason;
import java.util.List;

/** Một candidate đã qua toàn bộ bộ lọc quyền riêng tư và được xếp hạng tại database. */
public record StudentRecommendationResponse(
        Long userId,
        String username,
        String displayName,
        String avatarUrl,
        StudentRecommendationAcademicResponse academic,
        int matchScore,
        List<StudentMatchReason> matchReasons,
        int commonInterestCount,
        int mutualConnectionCount,
        boolean followedByMe
) {
}
