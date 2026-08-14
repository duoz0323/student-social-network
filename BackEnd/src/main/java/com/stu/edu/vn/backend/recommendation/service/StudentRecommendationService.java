package com.stu.edu.vn.backend.recommendation.service;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.recommendation.dto.response.StudentRecommendationResponse;

public interface StudentRecommendationService {
    PageResponse<StudentRecommendationResponse> getStudentRecommendations(int page, int size);
}
