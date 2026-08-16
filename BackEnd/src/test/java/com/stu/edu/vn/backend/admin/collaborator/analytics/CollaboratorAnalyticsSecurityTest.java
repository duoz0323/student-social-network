package com.stu.edu.vn.backend.admin.collaborator.analytics;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.security.AdminAuthorization;
import com.stu.edu.vn.backend.security.AuthRateLimiter;
import com.stu.edu.vn.backend.security.JwtAuthenticationFilter;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.SecurityConfig;
import com.stu.edu.vn.backend.security.SecurityCorsProperties;
import com.stu.edu.vn.backend.security.SecurityErrorResponseWriter;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CollaboratorAnalyticsController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class,
        SecurityCorsProperties.class, GlobalExceptionHandler.class})
class CollaboratorAnalyticsSecurityTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private CollaboratorAnalyticsService service;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private UserProfileRepository userProfileRepository;
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;

    @Test
    void userAndUserManagerWithoutPermissionAreForbidden() throws Exception {
        authenticate("user", user(9L, UserRole.USER), null);
        mockMvc.perform(get("/api/v1/admin/collaborator/dashboard")
                        .header("Authorization", "Bearer user"))
                .andExpect(status().isForbidden());

        authenticate("manager", user(10L, UserRole.ADMIN),
                new AdminAuthorization(Set.of("USER_MANAGER"), Set.of("USER_VIEW")));
        mockMvc.perform(get("/api/v1/admin/collaborator/dashboard")
                        .header("Authorization", "Bearer manager"))
                .andExpect(status().isForbidden());
    }

    @Test
    void collaboratorPermissionAllowsDashboard() throws Exception {
        authenticate("collaborator", user(15L, UserRole.ADMIN),
                new AdminAuthorization(Set.of("COLLABORATOR"), Set.of("COLLABORATOR_DASHBOARD_VIEW")));
        when(service.dashboard(30)).thenReturn(new CollaboratorDashboardResponse(0, 0, 0, 0,
                List.of(), List.of(), List.of()));

        mockMvc.perform(get("/api/v1/admin/collaborator/dashboard")
                        .header("Authorization", "Bearer collaborator"))
                .andExpect(status().isOk());
    }

    private void authenticate(String token, User user, AdminAuthorization authorization) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId())).thenReturn(true);
        if (authorization != null) {
            when(jwtService.extractAdminAuthorizationFromAccessToken(token)).thenReturn(authorization);
        }
    }

    private User user(Long id, UserRole role) {
        User user = new User("collaborator-security-" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        return user;
    }
}
