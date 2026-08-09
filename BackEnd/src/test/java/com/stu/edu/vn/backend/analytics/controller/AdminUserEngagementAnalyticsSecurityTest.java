package com.stu.edu.vn.backend.analytics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.analytics.dto.MonthlyUserEngagementItemResponse;
import com.stu.edu.vn.backend.analytics.dto.DashboardUserEngagementResponse;
import com.stu.edu.vn.backend.analytics.service.UserEngagementAnalyticsService;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUserEngagementAnalyticsController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class,
        SecurityCorsProperties.class, GlobalExceptionHandler.class})
class AdminUserEngagementAnalyticsSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserEngagementAnalyticsService analyticsService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private UserProfileRepository userProfileRepository;
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;

    @Test
    void noTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void regularUserReturns403() throws Exception {
        authenticate("user", user(9L, UserRole.USER));

        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/summary")
                        .header("Authorization", "Bearer user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeAdminCanAccessAnalytics() throws Exception {
        authenticate("admin", user(1L, UserRole.ADMIN));
        when(analyticsService.getSummary(null, 15)).thenReturn(emptyItem());

        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/summary")
                        .header("Authorization", "Bearer admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.month").value("2026-06"));
    }

    @Test
    void dashboardRequiresAdminRole() throws Exception {
        authenticate("user", user(9L, UserRole.USER));

        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/dashboard")
                        .header("Authorization", "Bearer user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        authenticate("admin", user(1L, UserRole.ADMIN));
        when(analyticsService.getDashboard(30)).thenReturn(new DashboardUserEngagementResponse(
                LocalDate.of(2026, 5, 17), LocalDate.of(2026, 6, 15), List.of(), List.of()));

        mockMvc.perform(get("/api/v1/admin/analytics/user-engagement/dashboard")
                        .header("Authorization", "Bearer admin"))
                .andExpect(status().isOk());
    }

    private void authenticate(String token, User user) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId())).thenReturn(true);
    }

    private User user(Long id, UserRole role) {
        User user = new User("analytics-security-" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private MonthlyUserEngagementItemResponse emptyItem() {
        return new MonthlyUserEngagementItemResponse(
                YearMonth.of(2026, 6), LocalDate.of(2026, 6, 15),
                0, 0, null, 0, 0, null, 0, 0, 0, 0, 0, null, 0, null, 0
        );
    }
}
