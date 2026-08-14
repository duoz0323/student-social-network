package com.stu.edu.vn.backend.recommendation.dto.response;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;

/** Thông tin học thuật công khai tối thiểu để trình bày lý do gợi ý. */
public record StudentRecommendationAcademicResponse(
        SchoolResponse school,
        AcademicItemResponse faculty,
        AcademicItemResponse major,
        Integer entryYear
) {
}
