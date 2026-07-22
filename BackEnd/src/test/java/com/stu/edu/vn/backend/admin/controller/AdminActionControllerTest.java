package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.dto.response.AdminActionAdminResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionListResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionTargetResponse;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.service.AdminActionService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AdminActionControllerTest {
    private final AdminActionService service = org.mockito.Mockito.mock(AdminActionService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminActionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void listUsesDefaultPaginationAndDoesNotExposeDetailJson() throws Exception {
        AdminActionListResponse item = listItem();
        when(service.getActions(null, null, null, null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/admin/actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.content[0].actionId").value(9))
                .andExpect(jsonPath("$.data.content[0].actionLabel").value("Khóa tài khoản"))
                .andExpect(jsonPath("$.data.content[0].admin.displayName").value("Quản trị viên"))
                .andExpect(jsonPath("$.data.content[0].target.targetAvailable").value(true))
                .andExpect(jsonPath("$.data.content[0].oldData").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].newData").doesNotExist());
        verify(service).getActions(null, null, null, null, null, 0, 20);
    }

    @Test
    void listPassesEveryFilterAndBoundaryPageSize() throws Exception {
        LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 16, 23, 59);
        when(service.getActions(AdminActionType.HIDE_POST, AdminTargetType.POST, 1L,
                from, to, 2, 100)).thenReturn(new PageResponse<>(List.of(), 2, 100, 0, 0, false, true));

        mockMvc.perform(get("/api/v1/admin/actions")
                        .param("actionType", "HIDE_POST").param("targetType", "POST")
                        .param("adminId", "1").param("from", "2026-07-01T00:00:00")
                        .param("to", "2026-07-16T23:59:00").param("page", "2").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        verify(service).getActions(AdminActionType.HIDE_POST, AdminTargetType.POST, 1L,
                from, to, 2, 100);
    }

    @Test
    void listRejectsInvalidPaginationEnumsAdminAndTimeRange() throws Exception {
        mockMvc.perform(get("/api/v1/admin/actions").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/actions").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/actions").param("adminId", "0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/actions").param("actionType", "INVALID"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/actions").param("targetType", "INVALID"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/admin/actions")
                        .param("from", "2026-07-17T00:00:00").param("to", "2026-07-16T00:00:00"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void detailReturnsJsonSnapshotsAndMapsMissingAction() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 16, 8, 0);
        AdminActionDetailResponse detail = new AdminActionDetailResponse(
                9L, AdminActionType.BLOCK_USER, "Khóa tài khoản",
                new AdminActionAdminResponse(1L, "Quản trị viên", "admin.jpg"),
                new AdminActionTargetResponse(AdminTargetType.USER, 10L, "Minh", true),
                "SPAM", createdAt, Map.of("status", "ACTIVE"), Map.of("status", "BLOCKED")
        );
        when(service.getActionDetail(9L)).thenReturn(detail);
        when(service.getActionDetail(404L))
                .thenThrow(new BusinessException(ErrorCode.ADMIN_ACTION_NOT_FOUND));

        mockMvc.perform(get("/api/v1/admin/actions/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.oldData.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.newData.status").value("BLOCKED"));
        mockMvc.perform(get("/api/v1/admin/actions/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADMIN_ACTION_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/admin/actions/0"))
                .andExpect(status().isBadRequest());
    }

    private AdminActionListResponse listItem() {
        return new AdminActionListResponse(
                9L, AdminActionType.BLOCK_USER, "Khóa tài khoản",
                new AdminActionAdminResponse(1L, "Quản trị viên", "admin.jpg"),
                new AdminActionTargetResponse(AdminTargetType.USER, 10L, "Minh", true),
                "SPAM", LocalDateTime.of(2026, 7, 16, 8, 0)
        );
    }
}
