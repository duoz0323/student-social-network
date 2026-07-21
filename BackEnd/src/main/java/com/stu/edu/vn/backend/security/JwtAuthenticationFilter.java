package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter xác thực Bearer Access Token và đưa người dùng hiện tại vào SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
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

            CustomUserPrincipal principal = new CustomUserPrincipal(user.getId(), user.getRole(), user.getStatus());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException exception) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, request, ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtAuthenticationException exception) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, request, exception.getErrorCode());
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, request, ErrorCode.INVALID_ACCESS_TOKEN);
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

    private void writeErrorResponse(
            HttpServletResponse response,
            HttpServletRequest request,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(toErrorJson(errorCode, request.getRequestURI()));
    }

    private String toErrorJson(ErrorCode errorCode, String path) {
        return """
                {"success":false,"code":"%s","message":"%s","status":%d,"path":"%s","fieldErrors":[],"timestamp":"%s"}"""
                .formatted(
                        escapeJson(errorCode.name()),
                        escapeJson(errorCode.getDefaultMessage()),
                        errorCode.getHttpStatus().value(),
                        escapeJson(path),
                        LocalDateTime.now()
                );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
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
