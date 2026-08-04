package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.service.AdminUserService;
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
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class, SecurityCorsProperties.class, GlobalExceptionHandler.class})
class AdminUserSecurityTest {
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;
    @MockitoBean private UserProfileRepository userProfileRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void activeUserTokenReturns403() throws Exception {
        authenticate("user-token", user(10L, UserRole.USER, UserStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeUserCannotUpdateAnotherUsersProfile() throws Exception {
        authenticate("user-update-token", user(10L, UserRole.USER, UserStatus.ACTIVE));

        mockMvc.perform(put("/api/v1/admin/users/20/profile")
                        .header("Authorization", "Bearer user-update-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Tên mới\",\"dateOfBirth\":\"2001-06-15\",\"bio\":\"Bio\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeAdminTokenCanAccessAdminEndpoint() throws Exception {
        authenticate("admin-token", user(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(adminUserService.getUsers(null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void blockedAdminIsRejectedByJwtFilter() throws Exception {
        authenticate("blocked-admin-token", user(1L, UserRole.ADMIN, UserStatus.BLOCKED));

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer blocked-admin-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_BLOCKED"));
    }

    private void authenticate(String token, User user) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId())).thenReturn(true);
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("user" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
