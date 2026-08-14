package com.stu.edu.vn.backend.admin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.admin.service.AdminAcademicService;
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

/** Chứng minh toàn bộ Admin Academic API kế thừa đúng 401/403/ADMIN guard. */
@WebMvcTest(AdminAcademicController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class,
        SecurityCorsProperties.class, GlobalExceptionHandler.class})
class AdminAcademicSecurityTest {
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;
    @MockitoBean private UserProfileRepository userProfileRepository;
    @MockitoBean private AdminAcademicService service;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    @Autowired private MockMvc mockMvc;

    @Test
    void noTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/academic/schools"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void userMutationReturns403() throws Exception {
        authenticate("user", user(9L, UserRole.USER, UserStatus.ACTIVE));
        mockMvc.perform(post("/api/v1/admin/academic/schools")
                        .header("Authorization", "Bearer user")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"School\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeAdminCanAccess() throws Exception {
        authenticate("admin", user(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(service.getSchools(null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));
        mockMvc.perform(get("/api/v1/admin/academic/schools")
                        .header("Authorization", "Bearer admin"))
                .andExpect(status().isOk());
    }

    private void authenticate(String token, User user) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId())).thenReturn(true);
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("academic" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
