package com.stu.edu.vn.backend.analytics.tracking;

import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Chỉ ghi nhận các request nghiệp vụ đại diện đã hoàn tất thành công, không theo dõi Auth/Admin/background.
 */
@Component
public class UserActivityTrackingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserActivityTrackingFilter.class);
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Pattern POST_DETAIL = Pattern.compile("^/api/v1/posts/\\d+$");
    private static final Pattern USER_FOLLOW = Pattern.compile("^/api/v1/users/\\d+/follow$");
    private final UserActivityTrackingService trackingService;
    private final Clock clock;

    @Autowired
    public UserActivityTrackingFilter(
            ObjectProvider<UserActivityTrackingService> trackingServiceProvider,
            ObjectProvider<Clock> clockProvider
    ) {
        // WebMvc test slice không nạp repository/service; runtime đầy đủ luôn cung cấp cả hai bean này.
        this.trackingService = trackingServiceProvider.getIfAvailable();
        this.clock = clockProvider.getIfAvailable(Clock::systemUTC);
    }

    UserActivityTrackingFilter(UserActivityTrackingService trackingService, Clock clock) {
        this.trackingService = trackingService;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (trackingService == null || response.getStatus() < 200 || response.getStatus() >= 300
                || !isRepresentativeActivity(request)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)
                || principal.getRole() != UserRole.USER) {
            return;
        }

        try {
            trackingService.track(principal.getUserId(), LocalDateTime.now(clock));
        } catch (RuntimeException exception) {
            // Analytics là best-effort: cảnh báo có ngữ cảnh kỹ thuật tối thiểu nhưng không làm hỏng response chính.
            LOGGER.warn("Không thể ghi nhận user activity cho route {}", request.getRequestURI(), exception);
        }
    }

    boolean isRepresentativeActivity(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("GET".equals(method)) {
            return path.startsWith("/api/v1/feeds/") || POST_DETAIL.matcher(path).matches();
        }
        if (!MUTATING_METHODS.contains(method)) {
            return false;
        }
        return path.equals("/api/v1/posts")
                || path.startsWith("/api/v1/posts/")
                || path.startsWith("/api/v1/comments/")
                || USER_FOLLOW.matcher(path).matches();
    }
}
