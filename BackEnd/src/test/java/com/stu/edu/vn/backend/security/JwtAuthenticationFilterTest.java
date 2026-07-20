package com.stu.edu.vn.backend.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final SecurityErrorResponseWriter errorResponseWriter =
            org.mockito.Mockito.mock(SecurityErrorResponseWriter.class);
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService, userRepository, errorResponseWriter);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicRegistrationSkipsExpiredOrInvalidAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/registrations");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtService, never()).extractUserIdFromAccessToken(anyString());
    }

    @Test
    void publicRegistrationVerificationSkipsJwtParsing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/auth/registrations/verify"
        );
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtService, never()).extractUserIdFromAccessToken(anyString());
    }

    @Test
    void publicLoginSkipsExpiredOrInvalidAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtService, never()).extractUserIdFromAccessToken(anyString());
    }

    @Test
    void publicRefreshAndLogoutSkipInvalidAuthorizationHeader() throws Exception {
        for (String path : java.util.List.of(
                "/api/v1/auth/refresh-token", "/api/v1/auth/logout", "/api/v1/auth/oauth/google"
        )) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
        verify(jwtService, never()).extractUserIdFromAccessToken(anyString());
    }

    @Test
    void publicRegistrationStatusGetSkipsJwtParsing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/auth/registrations/status"
        );
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtService, never()).extractUserIdFromAccessToken(anyString());
    }

    @Test
    void protectedEndpointStoresActiveUserInSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me/onboarding");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);
        User user = new User("student@example.com", null, "hash");
        ReflectionTestUtils.setField(user, "id", 10L);
        when(jwtService.extractUserIdFromAccessToken("valid-token")).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        org.assertj.core.api.Assertions.assertThat(principal.getUserId()).isEqualTo(10L);
    }

    @Test
    void protectedEndpointReturnsInvalidAccessTokenWithoutCallingController() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me/onboarding");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);
        when(jwtService.extractUserIdFromAccessToken("invalid-token")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, chain);

        verify(errorResponseWriter).write(response, request, ErrorCode.INVALID_ACCESS_TOKEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void reauthenticationEndpointDoesNotBypassJwtValidation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/reauthenticate");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);
        when(jwtService.extractUserIdFromAccessToken("invalid-token")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, chain);

        verify(errorResponseWriter).write(response, request, ErrorCode.INVALID_ACCESS_TOKEN);
        verify(chain, never()).doFilter(request, response);
    }
}
