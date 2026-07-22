package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.service.AdminPostService;
import com.stu.edu.vn.backend.admin.dto.request.AdminHidePostRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostStatusResponse;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.security.JwtAuthenticationFilter;
import com.stu.edu.vn.backend.security.AuthRateLimiter;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.SecurityConfig;
import com.stu.edu.vn.backend.security.SecurityCorsProperties;
import com.stu.edu.vn.backend.security.SecurityErrorResponseWriter;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

@WebMvcTest(AdminPostController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class, SecurityCorsProperties.class, GlobalExceptionHandler.class})
class AdminPostSecurityTest {
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;
    @MockitoBean private UserProfileRepository userProfileRepository;
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AdminPostService service;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;

    @Test
    void noTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void activeUserReturns403() throws Exception {
        authenticate("user", user(9L, UserRole.USER, UserStatus.ACTIVE));
        mockMvc.perform(get("/api/v1/admin/posts").header("Authorization", "Bearer user"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeAdminCanAccess() throws Exception {
        authenticate("admin", user(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(service.getPosts(null, null, null, false, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));
        mockMvc.perform(get("/api/v1/admin/posts").header("Authorization", "Bearer admin"))
                .andExpect(status().isOk());
    }

    @Test
    void blockedAdminIsRejectedByJwtFilter() throws Exception {
        authenticate("blocked", user(1L, UserRole.ADMIN, UserStatus.BLOCKED));
        mockMvc.perform(get("/api/v1/admin/posts").header("Authorization", "Bearer blocked"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("USER_BLOCKED"));
    }

    @Test
    void hideWithoutTokenReturns401AndActiveUserReturns403() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/posts/11/hide")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reasonCode\":\"SPAM\"}"))
                .andExpect(status().isUnauthorized());

        authenticate("hide-user", user(9L, UserRole.USER, UserStatus.ACTIVE));
        mockMvc.perform(patch("/api/v1/admin/posts/11/hide")
                        .header("Authorization", "Bearer hide-user")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reasonCode\":\"SPAM\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeAdminCanHideAndRestore() throws Exception {
        authenticate("mutation-admin", user(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(service.hidePost(11L, new AdminHidePostRequest(AdminPostHideReason.SPAM)))
                .thenReturn(new AdminPostStatusResponse(11L, PostStatus.HIDDEN, null, "SPAM", null, null));
        when(service.restorePost(11L))
                .thenReturn(new AdminPostStatusResponse(11L, PostStatus.PUBLISHED, null, null, null, null));

        mockMvc.perform(patch("/api/v1/admin/posts/11/hide")
                        .header("Authorization", "Bearer mutation-admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reasonCode\":\"SPAM\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/admin/posts/11/restore")
                        .header("Authorization", "Bearer mutation-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void blockedAdminCannotHide() throws Exception {
        authenticate("hide-blocked-admin", user(1L, UserRole.ADMIN, UserStatus.BLOCKED));
        mockMvc.perform(patch("/api/v1/admin/posts/11/hide")
                        .header("Authorization", "Bearer hide-blocked-admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reasonCode\":\"SPAM\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("USER_BLOCKED"));
    }

    private void authenticate(String token, User user) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId())).thenReturn(true);
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("post" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
