package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.dto.request.AdminBlockUserRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserStatusResponse;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.service.AdminUserService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminUserControllerTest {

    private final AdminUserService adminUserService = org.mockito.Mockito.mock(AdminUserService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserController(adminUserService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void listUsesDefaultPaginationAndReturnsOnlyApprovedFields() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 14, 8, 0);
        var item = new AdminUserListItemResponse(
                10L, "Minh", "avatar", "minh@example.com",
                UserStatus.ACTIVE, true, timestamp);
        when(adminUserService.getUsers(null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.content[0].userId").value(10))
                .andExpect(jsonPath("$.data.content[0].profileCompleted").value(true))
                .andExpect(jsonPath("$.data.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].token").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].avatarPublicId").doesNotExist());
        verify(adminUserService).getUsers(null, null, 0, 20);
    }

    @Test
    void listPassesKeywordStatusAndBoundarySizes() throws Exception {
        when(adminUserService.getUsers("  Minh  ", UserStatus.BLOCKED, 1, 1))
                .thenReturn(new PageResponse<>(List.of(), 1, 1, 0, 0, false, true));
        when(adminUserService.getUsers(null, null, 0, 100))
                .thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, true, true));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("keyword", "  Minh  ").param("status", "BLOCKED")
                        .param("page", "1").param("size", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/users").param("size", "100"))
                .andExpect(status().isOk());

        verify(adminUserService).getUsers("  Minh  ", UserStatus.BLOCKED, 1, 1);
        verify(adminUserService).getUsers(null, null, 0, 100);
    }

    @Test
    void listRejectsInvalidPaginationAndStatus() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/users").param("size", "0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/users").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/users").param("status", "DELETED"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void listReturnsEmptyPageWithHttp200() throws Exception {
        when(adminUserService.getUsers("missing", null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/api/v1/admin/users").param("keyword", "missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void detailReturnsApprovedFieldsWithoutSensitiveData() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 14, 8, 0);
        var detail = new AdminUserDetailResponse(
                10L, "Minh", "avatar", "bio", "minh@example.com",
                UserStatus.ACTIVE, false, null, null, null, timestamp, timestamp);
        when(adminUserService.getUserDetail(10L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/admin/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.profileCompleted").value(false))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.avatarPublicId").doesNotExist());
    }

    @Test
    void detailMapsMissingAndAdminTargetErrors() throws Exception {
        when(adminUserService.getUserDetail(404L))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));
        when(adminUserService.getUserDetail(1L))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN));

        mockMvc.perform(get("/api/v1/admin/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADMIN_USER_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ADMIN_USER_MANAGEMENT_FORBIDDEN"));
    }

    @Test
    void blockAcceptsOnlyReasonCodeAndReturnsStatusResponse() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 14, 8, 0);
        var response = new AdminUserStatusResponse(10L, UserStatus.BLOCKED, now, "SPAM", now);
        when(adminUserService.blockUser(10L, new AdminBlockUserRequest(AdminBlockReason.SPAM)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/users/10/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"SPAM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.status").value("BLOCKED"))
                .andExpect(jsonPath("$.data.blockedReason").value("SPAM"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
        verify(adminUserService).blockUser(10L, new AdminBlockUserRequest(AdminBlockReason.SPAM));
    }

    @Test
    void blockRejectsNullInvalidReasonAndUnexpectedNoteWithHttp400() throws Exception {
        when(adminUserService.blockUser(10L, new AdminBlockUserRequest(null)))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_BLOCK_REASON_REQUIRED));

        mockMvc.perform(patch("/api/v1/admin/users/10/block")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reasonCode\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ADMIN_BLOCK_REASON_REQUIRED"));
        mockMvc.perform(patch("/api/v1/admin/users/10/block")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reasonCode\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(patch("/api/v1/admin/users/10/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reasonCode\":\"SPAM\",\"note\":\"free text\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void unblockHasNoRequestBodyAndReturnsActiveStatus() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 14, 8, 0);
        when(adminUserService.unblockUser(10L))
                .thenReturn(new AdminUserStatusResponse(10L, UserStatus.ACTIVE, null, null, now));

        mockMvc.perform(patch("/api/v1/admin/users/10/unblock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.blockedAt").doesNotExist())
                .andExpect(jsonPath("$.data.blockedReason").doesNotExist());
        verify(adminUserService).unblockUser(10L);
    }
}
