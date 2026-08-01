package com.stu.edu.vn.backend.notification.security;

import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Xác thực JWT tại STOMP CONNECT và giới hạn client vào các user destination realtime được phép.
 */
@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    static final String NOTIFICATION_DESTINATION = "/user/queue/notifications";
    static final String MESSAGING_DESTINATION = "/user/queue/messaging";
    private static final Set<String> ALLOWED_SUBSCRIPTION_DESTINATIONS = Set.of(
            NOTIFICATION_DESTINATION,
            MESSAGING_DESTINATION
    );
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor));
        } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscription(accessor);
        } else if (accessor.getCommand() == StompCommand.SEND) {
            // Realtime chỉ phân phối server -> client; mọi mutation nghiệp vụ tiếp tục đi qua REST.
            throw new AccessDeniedException("STOMP SEND is not supported");
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String token = extractBearerToken(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION));
        Long userId;
        try {
            userId = jwtService.extractUserIdFromAccessToken(token);
        } catch (RuntimeException exception) {
            // Không đưa token hoặc chi tiết parse JWT vào message/log.
            throw new BadCredentialsException("Invalid STOMP Access Token", exception);
        }

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new BadCredentialsException("Invalid STOMP Access Token");
        }
        User user = optionalUser.get();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("Account is not active");
        }
        if (!userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(userId)) {
            throw new InsufficientAuthenticationException("Profile is not completed");
        }

        CustomUserPrincipal principal = new CustomUserPrincipal(user.getId(), user.getRole(), user.getStatus());
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication)
                || !authentication.isAuthenticated()) {
            throw new InsufficientAuthenticationException("STOMP connection is not authenticated");
        }
        String destination = accessor.getDestination();
        if (destination == null || !ALLOWED_SUBSCRIPTION_DESTINATIONS.contains(destination)) {
            throw new AccessDeniedException("STOMP destination is not allowed");
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Missing STOMP Access Token");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new BadCredentialsException("Missing STOMP Access Token");
        }
        return token;
    }
}
