package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Chặn API nghiệp vụ chính cho tới khi người dùng hoàn tất hồ sơ theo contract Auth. */
@Component
@RequiredArgsConstructor
public class ProfileCompletionFilter extends OncePerRequestFilter {
    private static final List<String> ALLOWED_PREFIXES = List.of(
            "/api/v1/auth/",
            "/api/v1/users/me/onboarding",
            "/api/v1/users/me/avatar",
            "/api/v1/users/me/auth-providers",
            "/api/v1/academic/",
            "/api/v1/interests"
    );

    private final UserProfileRepository userProfileRepository;
    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || SecurityPaths.isPublic(request.getMethod(), path)
                || ALLOWED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }
        // USER gọi Admin API phải đi tiếp tới authorization để luôn nhận 403, không bị che bởi onboarding guard.
        if (request.getRequestURI().startsWith("/api/v1/admin/") && principal.getRole() != UserRole.ADMIN) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(principal.getUserId())) {
            errorResponseWriter.write(response, request, ErrorCode.PROFILE_NOT_COMPLETED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
