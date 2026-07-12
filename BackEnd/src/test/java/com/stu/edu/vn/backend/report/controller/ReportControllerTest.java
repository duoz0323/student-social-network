package com.stu.edu.vn.backend.report.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.report.dto.response.CreateReportResponse;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.service.ReportService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReportControllerTest {

    private final ReportService reportService = org.mockito.Mockito.mock(ReportService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReportReturnsCreatedAndOnlyPublicResponseFields() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 12, 10, 0);
        when(reportService.createPostReport(any(), any())).thenReturn(
                new CreateReportResponse(100L, 1L, ReportReason.SPAM, ReportStatus.PENDING, createdAt)
        );

        mockMvc.perform(post("/api/v1/posts/1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"SPAM\",\"description\":\"  Quang cao lap lai  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value(100))
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.reason").value("SPAM"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.reporter").doesNotExist())
                .andExpect(jsonPath("$.data.postContentSnapshot").doesNotExist());

        verify(reportService).createPostReport(any(), any());
    }

    @Test
    void createReportRejectsDescriptionLongerThanOneThousandCharacters() throws Exception {
        String description = "a".repeat(1001);

        mockMvc.perform(post("/api/v1/posts/1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"SPAM\",\"description\":\"" + description + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("description"));
    }

    @Test
    void createReportRejectsUnsupportedReasonEnum() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"COPYRIGHT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
