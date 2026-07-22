package com.stu.edu.vn.backend.post.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.common.exception.GlobalExceptionHandler;
import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionListResponse;
import com.stu.edu.vn.backend.post.service.HashtagService;
import com.stu.edu.vn.backend.security.JwtAuthenticationFilter;
import com.stu.edu.vn.backend.security.AuthRateLimiter;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.SecurityConfig;
import com.stu.edu.vn.backend.security.SecurityCorsProperties;
import com.stu.edu.vn.backend.security.SecurityErrorResponseWriter;
import com.stu.edu.vn.backend.user.entity.User;
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

@WebMvcTest(HashtagController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class, SecurityCorsProperties.class, GlobalExceptionHandler.class})
class HashtagSecurityTest {
    @MockitoBean private AuthRateLimiter authRateLimiter;
    @MockitoBean private ClientIpAddressResolver clientIpAddressResolver;
    @MockitoBean private UserProfileRepository userProfileRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HashtagService hashtagService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void requestWithoutAccessTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/hashtags/suggestions").param("keyword", "doan"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void activeUserAccessTokenCanReachSuggestionsEndpoint() throws Exception {
        User user = new User("student@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", 10L);
        user.setStatus(UserStatus.ACTIVE);
        when(jwtService.extractUserIdFromAccessToken("user-token")).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(true);
        when(hashtagService.getSuggestions("doan")).thenReturn(new HashtagSuggestionListResponse(
                "doan", "doan", false, List.of(), true));

        mockMvc.perform(get("/api/v1/hashtags/suggestions")
                        .header("Authorization", "Bearer user-token")
                        .param("keyword", "doan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
