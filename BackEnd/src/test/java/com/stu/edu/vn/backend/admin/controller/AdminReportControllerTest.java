package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.request.AdminRejectReportRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminResolveReportRequest;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.dto.response.AdminReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportEvidenceResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportPostSummaryResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportResolutionResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusPostResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportUserResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportedPostResponse;
import com.stu.edu.vn.backend.admin.service.AdminReportService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.MediaType;

class AdminReportControllerTest {
    private final AdminReportService service = org.mockito.Mockito.mock(AdminReportService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminReportController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void listUsesDefaultsReturnsNestedSafeContractAndEmptyPage() throws Exception {
        var item = listItem();
        when(service.getReports(null, null, null, null, null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.content[0].reportId").value(31))
                .andExpect(jsonPath("$.data.content[0].reporter.accountStatus").value("BLOCKED"))
                .andExpect(jsonPath("$.data.content[0].post.currentStatus").value("DELETED"))
                .andExpect(jsonPath("$.data.content[0].snapshotMediaCount").value(2))
                .andExpect(jsonPath("$.data.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].token").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].avatarPublicId").doesNotExist());
        verify(service).getReports(null, null, null, null, null, null, 0, 20);
    }

    @Test
    void listPassesAllFiltersSupportsBoundariesAndEmptyPage() throws Exception {
        when(service.getReports(ReportStatus.PENDING, ReportReason.SPAM, 11L, 12L, 13L,
                "  spam  ", 1, 1)).thenReturn(new PageResponse<>(List.of(), 1, 1, 0, 0, false, true));
        when(service.getReports(null, null, null, null, null, null, 0, 100))
                .thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, true, true));

        mockMvc.perform(get("/api/v1/admin/reports").param("status", "PENDING")
                        .param("reason", "SPAM").param("postId", "11")
                        .param("reporterId", "12").param("authorId", "13")
                        .param("keyword", "  spam  ").param("page", "1").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isEmpty());
        mockMvc.perform(get("/api/v1/admin/reports").param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void listRejectsInvalidPaginationIdsStatusAndReason() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports").param("page", "-1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/reports").param("size", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/reports").param("size", "101")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/reports").param("postId", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/reports").param("reporterId", "-1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/reports").param("authorId", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/reports").param("status", "INVALID"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/reports").param("reason", "INVALID"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void detailReturnsSnapshotCurrentStateResolutionAndNoSensitiveData() throws Exception {
        when(service.getReportDetail(31L)).thenReturn(detail());

        mockMvc.perform(get("/api/v1/admin/reports/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.reportedPost.currentStatus").value("HIDDEN"))
                .andExpect(jsonPath("$.data.evidence.contentSnapshot").value("old content"))
                .andExpect(jsonPath("$.data.evidence.mediaSnapshot[0]").value("old.jpg"))
                .andExpect(jsonPath("$.data.resolution.resolvedBy.adminId").value(1))
                .andExpect(jsonPath("$.data.reporter.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.reportedPost.author.avatarPublicId").doesNotExist())
                .andExpect(jsonPath("$.data.token").doesNotExist());
    }

    @Test
    void detailMissingAndInvalidIdUseApprovedErrors() throws Exception {
        when(service.getReportDetail(404L)).thenThrow(new BusinessException(ErrorCode.ADMIN_REPORT_NOT_FOUND));
        mockMvc.perform(get("/api/v1/admin/reports/404")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADMIN_REPORT_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/admin/reports/0")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectAcceptsOnlyNoteAndReturnsCompactStatus() throws Exception {
        var response = statusResponse(ReportStatus.REJECTED, PostStatus.PUBLISHED, null);
        when(service.rejectReport(31L, new AdminRejectReportRequest("Không vi phạm"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/reports/31/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"Không vi phạm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.resolvedBy.adminId").value(1))
                .andExpect(jsonPath("$.data.post.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.evidence").doesNotExist())
                .andExpect(jsonPath("$.data.snapshot").doesNotExist());
    }

    @Test
    void resolveWithHidePassesEnumAndReturnsHiddenPost() throws Exception {
        var request = new AdminResolveReportRequest("Hợp lệ", true, AdminPostHideReason.SPAM);
        when(service.resolveReport(31L, request))
                .thenReturn(statusResponse(ReportStatus.RESOLVED, PostStatus.HIDDEN, "SPAM"));

        mockMvc.perform(patch("/api/v1/admin/reports/31/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resolutionNote":"Hợp lệ","hidePost":true,"hideReasonCode":"SPAM"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.post.status").value("HIDDEN"))
                .andExpect(jsonPath("$.data.post.hiddenReason").value("SPAM"));
        verify(service).resolveReport(31L, request);
    }

    @Test
    void mutationMapsValidationStateAndInvalidEnumErrors() throws Exception {
        when(service.rejectReport(31L, new AdminRejectReportRequest("   ")))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_REPORT_RESOLUTION_NOTE_REQUIRED));
        when(service.resolveReport(31L,
                new AdminResolveReportRequest("note", true, null)))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_REPORT_HIDE_REASON_REQUIRED));
        when(service.resolveReport(32L,
                new AdminResolveReportRequest("note", false, AdminPostHideReason.SPAM)))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_REPORT_HIDE_REASON_NOT_ALLOWED));
        when(service.rejectReport(33L, new AdminRejectReportRequest("note")))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_REPORT_ALREADY_PROCESSED));

        mockMvc.perform(patch("/api/v1/admin/reports/31/reject").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ADMIN_REPORT_RESOLUTION_NOTE_REQUIRED"));
        mockMvc.perform(patch("/api/v1/admin/reports/31/resolve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"note\",\"hidePost\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ADMIN_REPORT_HIDE_REASON_REQUIRED"));
        mockMvc.perform(patch("/api/v1/admin/reports/32/resolve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"note\",\"hidePost\":false,\"hideReasonCode\":\"SPAM\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ADMIN_REPORT_HIDE_REASON_NOT_ALLOWED"));
        mockMvc.perform(patch("/api/v1/admin/reports/33/reject").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"note\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADMIN_REPORT_ALREADY_PROCESSED"));
        mockMvc.perform(patch("/api/v1/admin/reports/31/resolve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"note\",\"hidePost\":true,\"hideReasonCode\":\"INVALID\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private AdminReportListItemResponse listItem() {
        return new AdminReportListItemResponse(31L, ReportStatus.PENDING, ReportReason.SPAM, null,
                new AdminReportUserResponse(12L, "Reporter", "avatar", UserStatus.BLOCKED),
                new AdminReportPostSummaryResponse(11L, PostStatus.DELETED, "snapshot", 13L,
                        "Author", UserStatus.BLOCKED),
                2, LocalDateTime.of(2026, 7, 15, 8, 0));
    }

    private AdminReportDetailResponse detail() {
        var reporter = new AdminReportUserResponse(12L, "Reporter", "reporter.jpg", UserStatus.ACTIVE);
        var author = new AdminReportUserResponse(13L, "Author", "author.jpg", UserStatus.BLOCKED);
        return new AdminReportDetailResponse(31L, ReportStatus.RESOLVED, ReportReason.SPAM, "description",
                reporter, new AdminReportedPostResponse(11L, PostStatus.HIDDEN, "new content", author,
                        LocalDateTime.now(), "SPAM", null),
                new AdminReportEvidenceResponse("old content", List.of("old.jpg")),
                new AdminReportResolutionResponse(
                        new com.stu.edu.vn.backend.admin.dto.response.AdminReportResolvedByResponse(1L, "Admin"),
                        LocalDateTime.now(), "valid"), LocalDateTime.now());
    }

    private AdminReportStatusResponse statusResponse(
            ReportStatus reportStatus, PostStatus postStatus, String hiddenReason) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 15, 8, 0);
        return new AdminReportStatusResponse(31L, reportStatus, now, "note",
                new com.stu.edu.vn.backend.admin.dto.response.AdminReportResolvedByResponse(1L, "Admin"),
                new AdminReportStatusPostResponse(11L, postStatus,
                        postStatus == PostStatus.HIDDEN ? now : null, hiddenReason));
    }
}
