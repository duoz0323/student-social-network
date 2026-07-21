package com.stu.edu.vn.backend.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stu.edu.vn.backend.auth.dto.*;
import com.stu.edu.vn.backend.auth.service.PasswordRecoveryService;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PasswordRecoveryControllerTest {
    private final PasswordRecoveryService service = org.mockito.Mockito.mock(PasswordRecoveryService.class);
    private MockMvc mvc;
    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new PasswordRecoveryController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void startReturnsNeutralAcceptedContract() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 10, 0);
        when(service.start(any())).thenReturn(new PasswordRecoveryChallengeResponse(true, "PASSWORD_RECOVERY",
                "recovery-token", now.plusMinutes(10), now.plusMinutes(1), now.plusMinutes(15)));
        mvc.perform(post("/api/v1/auth/password-recovery").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@example.com\"}"))
                .andExpect(status().isAccepted()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.recoveryFlowToken").value("recovery-token"))
                .andExpect(jsonPath("$.data.flowToken").doesNotExist());
    }
    @Test void verifyReadsTokenOnlyFromHeaderAndReturnsAuthorizedToken() throws Exception {
        when(service.verify(eq("recovery-token"), any())).thenReturn(
                new VerifyPasswordRecoveryResponse("reset-token", LocalDateTime.now().plusMinutes(5)));
        mvc.perform(post("/api/v1/auth/password-recovery/verify")
                        .header("X-Auth-Flow-Token", "recovery-token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetAuthorizedToken").value("reset-token"))
                .andExpect(jsonPath("$.data.resetToken").doesNotExist());
    }
    @Test void completeRejectsMissingHeaderThroughServiceWithoutLeakingTechnicalDetails() throws Exception {
        when(service.complete(isNull(), any())).thenThrow(new com.stu.edu.vn.backend.common.exception.BusinessException(
                com.stu.edu.vn.backend.common.exception.ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID));
        mvc.perform(post("/api/v1/auth/password-recovery/complete").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"NewStrong1!\",\"confirmPassword\":\"NewStrong1!\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_PASSWORD_RESET_TOKEN_INVALID"));
    }
}

