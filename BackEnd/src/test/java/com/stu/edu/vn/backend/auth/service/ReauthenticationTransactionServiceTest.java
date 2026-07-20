package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.config.ReauthenticationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class ReauthenticationTransactionServiceTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserAuthProviderRepository providerRepository =
            org.mockito.Mockito.mock(UserAuthProviderRepository.class);
    private final ReauthenticationChallengeRepository challengeRepository =
            org.mockito.Mockito.mock(ReauthenticationChallengeRepository.class);
    private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    private final FlowTokenGenerator tokenGenerator = org.mockito.Mockito.mock(FlowTokenGenerator.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), ZoneOffset.UTC);

    private AuthHmacService hmacService;
    private ReauthenticationTransactionService service;

    @BeforeEach
    void setUp() {
        AuthRegistrationProperties authProperties = new AuthRegistrationProperties();
        authProperties.setOtpHmacSecret("otp-secret-for-tests-0123456789");
        authProperties.setFlowTokenHmacSecret("flow-secret-for-tests-0123456789");
        hmacService = new AuthHmacService(authProperties);
        ReauthenticationProperties properties = new ReauthenticationProperties();
        properties.setExpiration(Duration.ofMinutes(5));
        service = new ReauthenticationTransactionService(
                userRepository, providerRepository, challengeRepository, passwordEncoder,
                tokenGenerator, hmacService, properties, clock);
    }

    @Test
    void correctPasswordStoresOnlyHashAndBindsScopeTargetAndExpiration() {
        User user = user(7L, "hash");
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "hash")).thenReturn(true);
        when(challengeRepository.findByActiveUserScopeKeyForUpdate("7:UNLINK_AUTH_METHOD"))
                .thenReturn(Optional.empty());
        when(tokenGenerator.generate()).thenReturn("raw-reauthentication-token");
        when(challengeRepository.saveAndFlush(any(ReauthenticationChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReauthenticationChallengeCreation result = service.authenticatePassword(
                7L, "Password@1", ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.GOOGLE);

        ArgumentCaptor<ReauthenticationChallenge> captor =
                ArgumentCaptor.forClass(ReauthenticationChallenge.class);
        verify(challengeRepository).saveAndFlush(captor.capture());
        ReauthenticationChallenge saved = captor.getValue();
        assertThat(result.rawToken()).isEqualTo("raw-reauthentication-token");
        assertThat(result.method()).isEqualTo(ReauthenticationMethod.PASSWORD);
        assertThat(result.expiresAt()).isEqualTo(LocalDateTime.now(clock).plusMinutes(5));
        assertThat(saved.getTokenHash()).isEqualTo(hmacService.hashFlowToken("raw-reauthentication-token"));
        assertThat(saved.getTokenHash()).doesNotContain("raw-reauthentication-token");
        assertThat(saved.getUser().getId()).isEqualTo(7L);
        assertThat(saved.getScope()).isEqualTo(ReauthenticationScope.UNLINK_AUTH_METHOD);
        assertThat(saved.getTargetAuthMethod()).isEqualTo(AuthMethod.GOOGLE);
        assertThat(saved.getProofMethod()).isEqualTo(ReauthenticationProofMethod.LOCAL_PASSWORD);
        assertThat(saved.getStatus()).isEqualTo(ReauthenticationChallengeStatus.ACTIVE);
    }

    @Test
    void wrongPasswordAndSocialOnlyAccountNeverCreateChallenge() {
        User local = user(8L, "hash");
        when(userRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(local));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertError(() -> service.authenticatePassword(8L, "wrong",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.EMAIL),
                ErrorCode.AUTH_REAUTHENTICATION_CREDENTIAL_INVALID);

        User socialOnly = user(9L, null);
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(socialOnly));
        assertError(() -> service.authenticatePassword(9L, "Password@1",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.GOOGLE),
                ErrorCode.AUTH_REAUTHENTICATION_METHOD_UNAVAILABLE);

        verify(challengeRepository, never()).saveAndFlush(any(ReauthenticationChallenge.class));
    }

    @Test
    void blockedUserIsRejectedBeforeCredentialVerification() {
        User user = user(10L, "hash");
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(user));

        assertError(() -> service.authenticatePassword(10L, "Password@1",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.PHONE), ErrorCode.USER_BLOCKED);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(challengeRepository, never()).saveAndFlush(any(ReauthenticationChallenge.class));
    }

    @Test
    void providerProofMustBelongToCurrentUser() {
        User current = user(11L, null);
        User other = user(12L, null);
        UserAuthProvider otherLink = new UserAuthProvider(
                other, AuthProvider.GOOGLE, "google-sub", null, null);
        when(userRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(current));
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "google-sub"))
                .thenReturn(Optional.of(otherLink));

        assertError(() -> service.authenticateProvider(11L, AuthProvider.GOOGLE, "google-sub",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.EMAIL),
                ErrorCode.AUTH_REAUTHENTICATION_PROVIDER_NOT_LINKED);
        verify(challengeRepository, never()).saveAndFlush(any(ReauthenticationChallenge.class));
    }

    @Test
    void linkedFacebookProofCreatesChallengeAndMissingProviderIsRejected() {
        User current = user(14L, null);
        UserAuthProvider link = new UserAuthProvider(
                current, AuthProvider.FACEBOOK, "facebook-id", null, null);
        when(userRepository.findByIdForUpdate(14L)).thenReturn(Optional.of(current));
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.FACEBOOK, "facebook-id"))
                .thenReturn(Optional.of(link));
        when(challengeRepository.findByActiveUserScopeKeyForUpdate("14:UNLINK_AUTH_METHOD"))
                .thenReturn(Optional.empty());
        when(tokenGenerator.generate()).thenReturn("facebook-reauth-token");
        when(challengeRepository.saveAndFlush(any(ReauthenticationChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReauthenticationChallengeCreation result = service.authenticateProvider(
                14L, AuthProvider.FACEBOOK, "facebook-id",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.GOOGLE);

        assertThat(result.method()).isEqualTo(ReauthenticationMethod.FACEBOOK);
        ArgumentCaptor<ReauthenticationChallenge> captor =
                ArgumentCaptor.forClass(ReauthenticationChallenge.class);
        verify(challengeRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getProofMethod()).isEqualTo(ReauthenticationProofMethod.FACEBOOK);

        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "missing-sub"))
                .thenReturn(Optional.empty());
        assertError(() -> service.authenticateProvider(
                14L, AuthProvider.GOOGLE, "missing-sub",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.EMAIL),
                ErrorCode.AUTH_REAUTHENTICATION_PROVIDER_NOT_LINKED);
    }

    @Test
    void newChallengeCancelsPreviousActiveChallengeInSameUserScope() {
        User user = user(13L, "hash");
        ReauthenticationChallenge existing = ReauthenticationChallenge.start(
                user, "a".repeat(64), ReauthenticationProofMethod.LOCAL_PASSWORD,
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.EMAIL,
                LocalDateTime.now(clock).plusMinutes(2));
        when(userRepository.findByIdForUpdate(13L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "hash")).thenReturn(true);
        when(challengeRepository.findByActiveUserScopeKeyForUpdate("13:UNLINK_AUTH_METHOD"))
                .thenReturn(Optional.of(existing));
        when(tokenGenerator.generate()).thenReturn("replacement-token");
        when(challengeRepository.saveAndFlush(any(ReauthenticationChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.authenticatePassword(13L, "Password@1",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.FACEBOOK);

        assertThat(existing.getStatus()).isEqualTo(ReauthenticationChallengeStatus.CANCELLED);
        assertThat(existing.getTokenHash()).isNull();
        assertThat(existing.getActiveUserScopeKey()).isNull();
        verify(challengeRepository, org.mockito.Mockito.times(2))
                .saveAndFlush(any(ReauthenticationChallenge.class));
    }

    private User user(Long id, String passwordHash) {
        User user = new User("student" + id + "@example.com", null, passwordHash);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode errorCode) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
