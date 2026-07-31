package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.service.AdminModerationCaseService;
import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseActionRequest;
import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseNoViolationRequest;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.enums.ModerationCaseAction;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.security.AuthRateLimiter;
import com.stu.edu.vn.backend.security.JwtAuthenticationFilter;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.SecurityConfig;
import com.stu.edu.vn.backend.security.SecurityCorsProperties;
import com.stu.edu.vn.backend.security.SecurityErrorResponseWriter;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminModerationCaseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class,
        SecurityCorsProperties.class, GlobalExceptionHandler.class})
class AdminModerationCaseSecurityTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AdminModerationCaseService service;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private UserProfileRepository userProfileRepository;
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;

    @Test
    void noTokenReturns401AndUserReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/moderation-cases"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        authenticate("user", user(9L, UserRole.USER, UserStatus.ACTIVE));
        mockMvc.perform(get("/api/v1/admin/moderation-cases").header("Authorization", "Bearer user"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeAdminCanReadAndResolveWhileBlockedAdminIsRejected() throws Exception {
        authenticate("admin", user(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(service.getCases(null, null, null, null, null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));
        mockMvc.perform(get("/api/v1/admin/moderation-cases").header("Authorization", "Bearer admin"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/admin/moderation-cases/20/resolve-no-violation")
                        .header("Authorization", "Bearer admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
        verify(service).resolveNoViolation(20L, new ResolveModerationCaseNoViolationRequest(null));

        mockMvc.perform(patch("/api/v1/admin/moderation-cases/21/resolve-action")
                        .header("Authorization", "Bearer admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"HIDE_POST\",\"reasonCode\":\"SPAM\"}"))
                .andExpect(status().isOk());
        verify(service).resolveAction(21L, new ResolveModerationCaseActionRequest(
                ModerationCaseAction.HIDE_POST, AdminPostHideReason.SPAM, null));

        authenticate("blocked", user(2L, UserRole.ADMIN, UserStatus.BLOCKED));
        mockMvc.perform(get("/api/v1/admin/moderation-cases").header("Authorization", "Bearer blocked"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("USER_BLOCKED"));
    }

    private void authenticate(String token, User user) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId())).thenReturn(true);
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("case" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
