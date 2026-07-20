package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rate limit tập trung cho Auth nhạy cảm, không đọc hoặc lưu request body chứa credential. */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final Set<String> PUBLIC_SENSITIVE = Set.of(
            "/api/v1/auth/registrations", "/api/v1/auth/registrations/verify",
            "/api/v1/auth/registrations/resend", "/api/v1/auth/login",
            "/api/v1/auth/oauth/google", "/api/v1/auth/oauth/facebook",
            "/api/v1/auth/refresh-token");
    private final AuthRateLimiter limiter;
    private final ClientIpAddressResolver ipResolver;
    private final SecurityErrorResponseWriter errorWriter;

    public AuthRateLimitFilter(AuthRateLimiter limiter, ClientIpAddressResolver ipResolver,
            SecurityErrorResponseWriter errorWriter) {
        this.limiter = limiter;
        this.ipResolver = ipResolver;
        this.errorWriter = errorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean mutatingAuthRequest = "POST".equals(request.getMethod()) || "DELETE".equals(request.getMethod());
        if (!mutatingAuthRequest) return true;
        String uri = request.getRequestURI();
        return !PUBLIC_SENSITIVE.contains(uri)
                && !"/api/v1/auth/reauthenticate".equals(uri)
                && !uri.startsWith("/api/v1/users/me/auth-providers/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        RateLimitDecision decision = limiter.acquire(buildOpaqueKey(request));
        if (!decision.allowed()) {
            response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
            errorWriter.write(response, request, ErrorCode.AUTH_RATE_LIMITED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String buildOpaqueKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String subject = authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal user
                ? "user:" + user.getUserId() : "ip:" + String.valueOf(ipResolver.resolve(request));
        // Chỉ giữ digest của subject và endpoint; không đưa email, phone hoặc token vào key/bộ nhớ.
        return sha256(subject + "|" + request.getRequestURI());
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }
}
