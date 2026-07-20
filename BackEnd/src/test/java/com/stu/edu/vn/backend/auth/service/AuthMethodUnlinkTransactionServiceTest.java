package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthMethodUnlinkTransactionServiceTest {
    private final UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
    private final UserAuthProviderRepository providers = org.mockito.Mockito.mock(UserAuthProviderRepository.class);
    private final ReauthenticationChallengeRepository challenges =
            org.mockito.Mockito.mock(ReauthenticationChallengeRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), ZoneOffset.UTC);
    private AuthHmacService hmac;
    private AuthMethodUnlinkTransactionService service;

    @BeforeEach
    void setUp() {
        AuthRegistrationProperties properties = new AuthRegistrationProperties();
        properties.setOtpHmacSecret("otp-secret-for-tests-0123456789");
        properties.setFlowTokenHmacSecret("flow-secret-for-tests-0123456789");
        hmac = new AuthHmacService(properties);
        service = new AuthMethodUnlinkTransactionService(users, providers, challenges, hmac, clock);
    }

    @Test
    void removesGoogleWhenCompleteEmailRemainsAndConsumesChallenge() {
        User user = localUser(7L, true, false);
        UserAuthProvider google = provider(user, AuthProvider.GOOGLE);
        stub(user, AuthMethod.GOOGLE, List.of(google));
        when(providers.findByUserIdAndProviderForUpdate(7L, AuthProvider.GOOGLE))
                .thenReturn(Optional.of(google));

        service.unlink(7L, AuthMethod.GOOGLE, "token");

        verify(providers).delete(google);
        assertThat(challenge(AuthMethod.GOOGLE, user).getStatus())
                .isEqualTo(ReauthenticationChallengeStatus.ACTIVE);
        verify(challenges).saveAndFlush(any(ReauthenticationChallenge.class));
        assertThat(user.getEmail()).isNotNull();
        assertThat(user.getPasswordHash()).isEqualTo("hash");
    }

    @Test
    void removesEmailWithoutChangingPhoneAndKeepsSharedPassword() {
        User user = localUser(8L, true, true);
        stub(user, AuthMethod.EMAIL, List.of());

        service.unlink(8L, AuthMethod.EMAIL, "token");

        assertThat(user.getEmail()).isNull();
        assertThat(user.getEmailVerifiedAt()).isNull();
        assertThat(user.getPhoneNumber()).isEqualTo("+84900000008");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
    }

    @Test
    void removesLastLocalIdentifierWhenSocialRemainsAndClearsUnusedPassword() {
        User user = localUser(9L, true, false);
        UserAuthProvider facebook = provider(user, AuthProvider.FACEBOOK);
        stub(user, AuthMethod.EMAIL, List.of(facebook));

        service.unlink(9L, AuthMethod.EMAIL, "token");

        assertThat(user.getEmail()).isNull();
        assertThat(user.getPasswordHash()).isNull();
        verify(providers, never()).delete(any());
    }

    @Test
    void verifiedIdentifierWithoutPasswordDoesNotCountAsLoginMethod() {
        User user = localUser(10L, true, false);
        user.setPasswordHash(null);
        UserAuthProvider google = provider(user, AuthProvider.GOOGLE);
        stub(user, AuthMethod.GOOGLE, List.of(google));

        assertError(() -> service.unlink(10L, AuthMethod.GOOGLE, "token"),
                ErrorCode.AUTH_LAST_LOGIN_METHOD_CANNOT_BE_REMOVED);
        verify(providers, never()).delete(any());
        verify(challenges, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingChallengeWrongTargetBlockedUserAndUnlinkedMethod() {
        User user = localUser(11L, true, false);
        when(users.findByIdForUpdate(11L)).thenReturn(Optional.of(user));
        assertError(() -> service.unlink(11L, AuthMethod.EMAIL, null),
                ErrorCode.AUTH_REAUTHENTICATION_REQUIRED);

        user.setStatus(UserStatus.BLOCKED);
        assertError(() -> service.unlink(11L, AuthMethod.EMAIL, "token"), ErrorCode.USER_BLOCKED);

        user.setStatus(UserStatus.ACTIVE);
        ReauthenticationChallenge wrong = challenge(AuthMethod.GOOGLE, user);
        when(challenges.findByTokenHashForUpdate(hmac.hashFlowToken("token"))).thenReturn(Optional.of(wrong));
        assertError(() -> service.unlink(11L, AuthMethod.EMAIL, "token"),
                ErrorCode.AUTH_REAUTHENTICATION_PURPOSE_INVALID);

        stub(user, AuthMethod.PHONE, List.of(provider(user, AuthProvider.GOOGLE)));
        assertError(() -> service.unlink(11L, AuthMethod.PHONE, "token"), ErrorCode.AUTH_METHOD_NOT_LINKED);
    }

    private void stub(User user, AuthMethod target, List<UserAuthProvider> social) {
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        ReauthenticationChallenge challenge = challenge(target, user);
        when(challenges.findByTokenHashForUpdate(hmac.hashFlowToken("token")))
                .thenReturn(Optional.of(challenge));
        when(providers.findAllByUserIdOrderByProviderAsc(user.getId())).thenReturn(social);
    }

    private ReauthenticationChallenge challenge(AuthMethod target, User user) {
        return ReauthenticationChallenge.start(user, hmac.hashFlowToken("token"),
                ReauthenticationProofMethod.LOCAL_PASSWORD, ReauthenticationScope.UNLINK_AUTH_METHOD,
                target, LocalDateTime.now(clock).plusMinutes(5));
    }

    private User localUser(Long id, boolean email, boolean phone) {
        User user = new User(email ? "student" + id + "@example.com" : null,
                phone ? "+8490000000" + id : null, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        if (email) user.setEmailVerifiedAt(LocalDateTime.now(clock));
        if (phone) user.setPhoneVerifiedAt(LocalDateTime.now(clock));
        return user;
    }

    private UserAuthProvider provider(User user, AuthProvider provider) {
        return new UserAuthProvider(user, provider, provider.name().toLowerCase() + "-id", null, null);
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(code);
    }
}
