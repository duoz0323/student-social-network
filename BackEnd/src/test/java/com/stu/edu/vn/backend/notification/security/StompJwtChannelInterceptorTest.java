package com.stu.edu.vn.backend.notification.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

class StompJwtChannelInterceptorTest {

    private final JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository =
            org.mockito.Mockito.mock(UserProfileRepository.class);
    private final StompJwtChannelInterceptor interceptor =
            new StompJwtChannelInterceptor(jwtService, userRepository, userProfileRepository);

    @Test
    void connectWithoutTokenIsRejected() {
        assertThatThrownBy(() -> interceptor.preSend(message(StompCommand.CONNECT, null, null), null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void invalidOrExpiredAccessTokenIsRejected() {
        when(jwtService.extractUserIdFromAccessToken("invalid"))
                .thenThrow(new JwtException("invalid"));
        when(jwtService.extractUserIdFromAccessToken("expired"))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        assertThatThrownBy(() -> interceptor.preSend(connect("invalid"), null))
                .isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> interceptor.preSend(connect("expired"), null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshTokenCannotBeUsedForConnect() {
        when(jwtService.extractUserIdFromAccessToken("refresh-token"))
                .thenThrow(new JwtException("Invalid token type"));

        assertThatThrownBy(() -> interceptor.preSend(connect("refresh-token"), null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void blockedUserIsRejected() {
        User user = activeUser(10L);
        user.setStatus(UserStatus.BLOCKED);
        when(jwtService.extractUserIdFromAccessToken("valid")).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> interceptor.preSend(connect("valid"), null))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void incompleteProfileIsRejected() {
        when(jwtService.extractUserIdFromAccessToken("valid")).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser(10L)));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(connect("valid"), null))
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    @Test
    void validConnectUsesUserIdAsPrincipalName() {
        when(jwtService.extractUserIdFromAccessToken("valid")).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser(10L)));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(true);
        Message<byte[]> connect = connect("valid");

        interceptor.preSend(connect, null);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(connect);
        assertThat(accessor.getUser()).isInstanceOf(Authentication.class);
        assertThat(accessor.getUser().getName()).isEqualTo("10");
        assertThat(((Authentication) accessor.getUser()).getPrincipal())
                .isInstanceOf(CustomUserPrincipal.class);
    }

    @Test
    void clientCanSubscribeAllowedNotificationAndMessagingDestinations() {
        Authentication authentication = authenticatedUser(10L);

        assertThat(interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/user/queue/notifications", authentication), null))
                .isNotNull();
        assertThat(interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/user/queue/messaging", authentication), null))
                .isNotNull();
    }

    @Test
    void clientCannotSubscribeDestinationOutsideAllowlist() {
        Authentication authentication = authenticatedUser(10L);

        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, null, authentication), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/topic/notifications", authentication), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/queue/notifications", authentication), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/app/messaging", authentication), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void clientCannotSubscribeDestinationContainingUserId() {
        Authentication authentication = authenticatedUser(10L);

        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/queue/users/20/notifications", authentication), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/user/20/queue/notifications", authentication), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void clientCanSendOnlyAuthenticatedTypingDestination() {
        assertThat(interceptor.preSend(
                message(StompCommand.SEND, "/app/messaging/typing", authenticatedUser(10L)), null))
                .isNotNull();

        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SEND, "/app/messaging/typing", null), null))
                .isInstanceOf(InsufficientAuthenticationException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SEND, null, authenticatedUser(10L)), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SEND, "/app/notifications", authenticatedUser(10L)), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SEND, "/topic/messaging", authenticatedUser(10L)), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> interceptor.preSend(
                message(StompCommand.SEND, "/user/20/queue/messaging", authenticatedUser(10L)), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Message<byte[]> connect(String token) {
        return message(StompCommand.CONNECT, null, null, Map.of(
                HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private Message<byte[]> message(StompCommand command, String destination, java.security.Principal user) {
        return message(command, destination, user, Map.of());
    }

    private Message<byte[]> message(
            StompCommand command,
            String destination,
            java.security.Principal user,
            Map<String, String> nativeHeaders
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        if (destination != null) accessor.setDestination(destination);
        if (user != null) accessor.setUser(user);
        nativeHeaders.forEach(accessor::setNativeHeader);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Authentication authenticatedUser(Long id) {
        User user = activeUser(id);
        CustomUserPrincipal principal = new CustomUserPrincipal(id, user.getRole(), user.getStatus());
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }

    private User activeUser(Long id) {
        User user = new User("student" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
