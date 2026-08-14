package com.stu.edu.vn.backend.discovery.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationsResponse;
import com.stu.edu.vn.backend.discovery.service.DiscoveryMapService;
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

/** Chứng minh Map kế thừa JWT, account ACTIVE và onboarding guard chung của hệ thống. */
@WebMvcTest(DiscoveryMapController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class,
        SecurityCorsProperties.class, GlobalExceptionHandler.class})
class DiscoveryMapSecurityTest {
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;
    @MockitoBean private UserProfileRepository userProfileRepository;
    @MockitoBean private DiscoveryMapService service;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    @Autowired private MockMvc mockMvc;

    @Test
    void noTokenReturns401() throws Exception {
        mockMvc.perform(markerRequest())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void blockedAndIncompleteAccountsAreRejectedBeforeService() throws Exception {
        authenticate("blocked", user(8L, UserRole.USER, UserStatus.BLOCKED), true);
        mockMvc.perform(markerRequest().header("Authorization", "Bearer blocked"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_BLOCKED"));

        authenticate("incomplete", user(9L, UserRole.USER, UserStatus.ACTIVE), false);
        mockMvc.perform(markerRequest().header("Authorization", "Bearer incomplete"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_COMPLETED"));
    }

    @Test
    void eligibleUserCanAccessBothEndpoints() throws Exception {
        authenticate("eligible", user(10L, UserRole.USER, UserStatus.ACTIVE), true);
        when(service.getLocations(1.0d, 0.0d, 1.0d, 0.0d))
                .thenReturn(new MapLocationsResponse(List.of(), false));
        when(service.getLocationPosts(15L, 10, null))
                .thenReturn(new com.stu.edu.vn.backend.common.api.CursorPageResponse<>(List.of(), null, false));

        mockMvc.perform(markerRequest().header("Authorization", "Bearer eligible"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/discovery/map/locations/15/posts")
                        .header("Authorization", "Bearer eligible"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder markerRequest() {
        return get("/api/v1/discovery/map/locations")
                .param("north", "1").param("south", "0")
                .param("east", "1").param("west", "0");
    }

    private void authenticate(String token, User user, boolean completed) {
        when(jwtService.extractUserIdFromAccessToken(token)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(user.getId()))
                .thenReturn(completed);
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("map-security-" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
