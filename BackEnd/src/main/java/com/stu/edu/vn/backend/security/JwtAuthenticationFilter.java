package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter xác thực Bearer Access Token và đưa người dùng hiện tại vào SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityPaths.isPublic(request.getMethod(), request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long userId = jwtService.extractUserIdFromAccessToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new JwtAuthenticationException(ErrorCode.USER_NOT_FOUND));
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new JwtAuthenticationException(ErrorCode.USER_BLOCKED);
            }

            AdminAuthorization authorization = user.getRole() == UserRole.ADMIN
                    ? jwtService.extractAdminAuthorizationFromAccessToken(token)
                    : new AdminAuthorization(java.util.Set.of(), java.util.Set.of());
            CustomUserPrincipal principal = authorization == null
                    ? CustomUserPrincipal.legacyAdmin(user.getId(), user.getStatus())
                    : new CustomUserPrincipal(
                            user.getId(),
                            user.getRole(),
                            user.getStatus(),
                            authorization.roles(),
                            authorization.permissions()
                    );
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException exception) {
            SecurityContextHolder.clearContext();
            errorResponseWriter.write(response, request, ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtAuthenticationException exception) {
            SecurityContextHolder.clearContext();
            errorResponseWriter.write(response, request, exception.getErrorCode());
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            errorResponseWriter.write(response, request, ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    /**
     * Exception nội bộ giúp filter chọn đúng mã lỗi mà không để lộ chi tiết kỹ thuật.
     */
    private static class JwtAuthenticationException extends RuntimeException {

        private final ErrorCode errorCode;

        JwtAuthenticationException(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        ErrorCode getErrorCode() {
            return errorCode;
        }
    }
}
