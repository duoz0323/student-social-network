package com.stu.edu.vn.backend.recommendation.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.recommendation.dto.response.StudentRecommendationResponse;
import com.stu.edu.vn.backend.recommendation.service.StudentRecommendationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint read-only cho danh sách “Có thể bạn biết” của USER đang đăng nhập. */
@Validated
@RestController
@RequestMapping("/api/v1/recommendations")
public class StudentRecommendationController {

    private final StudentRecommendationService recommendationService;

    public StudentRecommendationController(StudentRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<PageResponse<StudentRecommendationResponse>>> getStudentRecommendations(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        PageResponse<StudentRecommendationResponse> response =
                recommendationService.getStudentRecommendations(page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy gợi ý sinh viên thành công", response));
    }
}
