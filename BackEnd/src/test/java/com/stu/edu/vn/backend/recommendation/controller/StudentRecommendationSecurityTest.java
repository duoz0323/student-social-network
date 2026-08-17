package com.stu.edu.vn.backend.recommendation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.recommendation.service.StudentRecommendationService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/** Kiểm chứng endpoint Recommendation kế thừa đúng JWT và onboarding guard chung. */
@WebMvcTest(StudentRecommendationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class,
        SecurityCorsProperties.class, GlobalExceptionHandler.class})
class StudentRecommendationSecurityTest {
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;
    @MockitoBean private UserProfileRepository userProfileRepository;
    @MockitoBean private StudentRecommendationService service;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    @Autowired private MockMvc mockMvc;

    @Test
    void noTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void incompleteProfileReturnsApprovedErrorBeforeService() throws Exception {
        authenticate("incomplete", user(9L), false);
        mockMvc.perform(get("/api/v1/recommendations/students")
                        .header("Authorization", "Bearer incomplete"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_COMPLETED"));
    }

    @Test
    void eligibleUserCanAccess() throws Exception {
        authenticate("eligible", user(10L), true);
        when(service.getStudentRecommendations(0, 10))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true, true));
        mockMvc.perform(get("/api/v1/recommendations/students")
                        .header("Authorization", "Bearer eligible"))
                .andExpect(status().isOk());
    }

    @Test
    void adminCannotUseSocialRecommendationEndpoint() throws Exception {
        User admin = user(11L);
        admin.setRole(UserRole.ADMIN);
        authenticate("admin", admin, true);

        mockMvc.perform(get("/api/v1/recommendations/students")
                        .header("Authorization", "Bearer admin"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private void authenticate(String token, User user, boolean completed) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId()))
                .thenReturn(completed);
    }

    private User user(Long id) {
        User user = new User("recommendation" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
