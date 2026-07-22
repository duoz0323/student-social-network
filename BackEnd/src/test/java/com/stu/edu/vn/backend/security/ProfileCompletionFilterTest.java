package com.stu.edu.vn.backend.security;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class ProfileCompletionFilterTest {
    private final UserProfileRepository profiles = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final SecurityErrorResponseWriter writer = org.mockito.Mockito.mock(SecurityErrorResponseWriter.class);
    private final FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);
    private final ProfileCompletionFilter filter = new ProfileCompletionFilter(profiles, writer);

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    @Test
    void blocksMainApiWhenProfileIsIncomplete() throws Exception {
        authenticate(7L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(profiles.existsByUserIdAndProfileCompletedAtIsNotNull(7L)).thenReturn(false);

        filter.doFilter(request, response, chain);

        verify(writer).write(response, request, ErrorCode.PROFILE_NOT_COMPLETED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowsMainApiWhenProfileIsComplete() throws Exception {
        authenticate(7L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(profiles.existsByUserIdAndProfileCompletedAtIsNotNull(7L)).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(writer, never()).write(response, request, ErrorCode.PROFILE_NOT_COMPLETED);
    }

    @Test
    void onboardingAndAuthMethodApisRemainAvailable() throws Exception {
        authenticate(7L);
        for (String path : java.util.List.of(
                "/api/v1/users/me/onboarding",
                "/api/v1/users/me/avatar",
                "/api/v1/users/me/auth-providers")) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }
        verify(chain, org.mockito.Mockito.times(3)).doFilter(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(profiles, never()).existsByUserIdAndProfileCompletedAtIsNotNull(7L);
    }

    @Test
    void userAdminRequestContinuesToRoleAuthorization() throws Exception {
        authenticate(7L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/users");

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(profiles, never()).existsByUserIdAndProfileCompletedAtIsNotNull(7L);
    }

    private void authenticate(Long userId) {
        var principal = new CustomUserPrincipal(userId, UserRole.USER, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
