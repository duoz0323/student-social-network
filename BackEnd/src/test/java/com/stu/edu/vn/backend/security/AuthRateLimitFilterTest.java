package com.stu.edu.vn.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.util.ClientIpAddressProperties;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

class AuthRateLimitFilterTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void excessRequestReturns429RetryAfterAndOpaqueKey() throws Exception {
        AuthRateLimiter limiter = org.mockito.Mockito.mock(AuthRateLimiter.class);
        when(limiter.acquire(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(RateLimitDecision.reject(42));
        ClientIpAddressProperties ipProperties = new ClientIpAddressProperties();
        AuthRateLimitFilter filter = new AuthRateLimitFilter(limiter,
                new ClientIpAddressResolver(ipProperties),
                new SecurityErrorResponseWriter(new ObjectMapper()));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("203.0.113.9");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("42");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getContentAsString()).contains("AUTH_RATE_LIMITED").doesNotContain("203.0.113.9");
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(limiter).acquire(key.capture());
        assertThat(key.getValue()).hasSize(64).doesNotContain("203.0.113.9");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowedRequestContinuesFilterChain() throws Exception {
        AuthRateLimiter limiter = org.mockito.Mockito.mock(AuthRateLimiter.class);
        when(limiter.acquire(org.mockito.ArgumentMatchers.anyString())).thenReturn(RateLimitDecision.allow());
        AuthRateLimitFilter filter = new AuthRateLimitFilter(limiter,
                new ClientIpAddressResolver(new ClientIpAddressProperties()),
                new SecurityErrorResponseWriter(new ObjectMapper()));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
