package com.stu.edu.vn.backend.recommendation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.recommendation.dto.response.StudentRecommendationAcademicResponse;
import com.stu.edu.vn.backend.recommendation.dto.response.StudentRecommendationResponse;
import com.stu.edu.vn.backend.recommendation.enums.StudentMatchReason;
import com.stu.edu.vn.backend.recommendation.service.StudentRecommendationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class StudentRecommendationControllerTest {

    private final StudentRecommendationService service = org.mockito.Mockito.mock(StudentRecommendationService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentRecommendationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void returnsDefaultPageAndOnlyPublicRecommendationFields() throws Exception {
        StudentRecommendationResponse item = new StudentRecommendationResponse(
                25L, "nguyenvana", "Nguyễn Văn A", null,
                new StudentRecommendationAcademicResponse(
                        new SchoolResponse(1L, "Đại học Công nghệ Sài Gòn", "STU"),
                        new AcademicItemResponse(2L, "Công nghệ Thông tin"),
                        new AcademicItemResponse(3L, "Công nghệ Thông tin"), 2022),
                100, List.of(StudentMatchReason.SAME_MAJOR, StudentMatchReason.COMMON_INTERESTS),
                3, 0, false);
        when(service.getStudentRecommendations(0, 10))
                .thenReturn(new PageResponse<>(List.of(item), 0, 10, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/recommendations/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].userId").value(25))
                .andExpect(jsonPath("$.data.content[0].username").value("nguyenvana"))
                .andExpect(jsonPath("$.data.content[0].academic.major.id").value(3))
                .andExpect(jsonPath("$.data.content[0].matchScore").value(100))
                .andExpect(jsonPath("$.data.content[0].followedByMe").value(false))
                .andExpect(jsonPath("$.data.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].dateOfBirth").doesNotExist())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
        verify(service).getStudentRecommendations(0, 10);
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/students").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/recommendations/students").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
