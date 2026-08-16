package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.GoogleAuthProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.google.SocialChallengeSecurity;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class SocialAuthenticationTransactionServiceTest {
    private final UserAuthProviderRepository providers = org.mockito.Mockito.mock(UserAuthProviderRepository.class);
    private final UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository profiles = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PendingRegistrationRepository pending = org.mockito.Mockito.mock(PendingRegistrationRepository.class);
    private final SocialAuthChallengeRepository challenges = org.mockito.Mockito.mock(SocialAuthChallengeRepository.class);
    private final RefreshTokenIssuer refreshIssuer = org.mockito.Mockito.mock(RefreshTokenIssuer.class);
    private final JwtService jwt = org.mockito.Mockito.mock(JwtService.class);
    private SocialAuthenticationTransactionService service;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenExpirationMillis(900_000);
        AtomicReference<User> saved = new AtomicReference<>();
        when(providers.findByProviderAndProviderUserIdForUpdate(any(), any())).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 70L);
            saved.set(user);
            return user;
        });
        when(profiles.saveAndFlush(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.findById(70L)).thenAnswer(invocation -> Optional.of(new UserProfile(saved.get())));
        when(providers.saveAndFlush(any(UserAuthProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshIssuer.issue(any(), any(), any(), any())).thenReturn(new IssuedRefreshToken("refresh", 3600));
        when(jwt.generateAccessToken(70L, "USER")).thenReturn("access");
        service = new SocialAuthenticationTransactionService(providers, users, profiles, pending, challenges,
                refreshIssuer, jwt, jwtProperties, new GoogleAuthProperties(),
                org.mockito.Mockito.mock(SocialChallengeSecurity.class),
                org.mockito.Mockito.mock(AuthHmacService.class),
                Clock.fixed(Instant.parse("2026-08-16T03:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void facebookEmailRemainsProviderMetadataAndDoesNotCreateLocalEmail() {
        service.authenticate(AuthProvider.FACEBOOK, "facebook-id", "Student@Example.com", true, true,
                null, null, null);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserAuthProvider> provider = ArgumentCaptor.forClass(UserAuthProvider.class);
        verify(users).saveAndFlush(user.capture());
        verify(providers).saveAndFlush(provider.capture());
        assertThat(user.getValue().getEmail()).isNull();
        assertThat(user.getValue().getEmailVerifiedAt()).isNull();
        assertThat(user.getValue().getPasswordHash()).isNull();
        assertThat(provider.getValue().getProviderEmail()).isEqualTo("student@example.com");
    }

    @Test
    void facebookWithoutEmailCreatesValidProviderOnlyAccount() {
        service.authenticate(AuthProvider.FACEBOOK, "facebook-id", null, null, true,
                null, null, null);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(user.capture());
        assertThat(user.getValue().getEmail()).isNull();
        assertThat(user.getValue().getPasswordHash()).isNull();
    }

    @Test
    void verifiedGoogleEmailPopulatesAccountButDoesNotSetPassword() {
        service.authenticate(AuthProvider.GOOGLE, "google-sub", "Student@Example.com", true, true,
                null, null, null);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(user.capture());
        assertThat(user.getValue().getEmail()).isEqualTo("student@example.com");
        assertThat(user.getValue().getEmailVerifiedAt()).isNotNull();
        assertThat(user.getValue().getPasswordHash()).isNull();
    }
}
