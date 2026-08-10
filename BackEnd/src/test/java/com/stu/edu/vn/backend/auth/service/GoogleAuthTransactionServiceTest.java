package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.GoogleAuthProperties;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthResponse;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.google.SocialChallengeSecurity;
import com.stu.edu.vn.backend.auth.google.VerifiedGoogleIdentity;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class GoogleAuthTransactionServiceTest {

    private final UserAuthProviderRepository providerRepository = org.mockito.Mockito.mock(UserAuthProviderRepository.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository profileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PendingRegistrationRepository pendingRepository = org.mockito.Mockito.mock(PendingRegistrationRepository.class);
    private final SocialAuthChallengeRepository challengeRepository = org.mockito.Mockito.mock(SocialAuthChallengeRepository.class);
    private final RefreshTokenIssuer refreshTokenIssuer = org.mockito.Mockito.mock(RefreshTokenIssuer.class);
    private final JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    private final SocialChallengeSecurity challengeSecurity = org.mockito.Mockito.mock(SocialChallengeSecurity.class);
    private final JwtProperties jwtProperties = new JwtProperties();
    private final GoogleAuthProperties googleProperties = new GoogleAuthProperties();
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T01:00:00Z"), ZoneOffset.UTC);
    private GoogleAuthTransactionService service;

    @BeforeEach
    void setUp() {
        jwtProperties.setAccessTokenExpirationMillis(900_000);
        googleProperties.setConflictExpiration(java.time.Duration.ofMinutes(5));
        service = new GoogleAuthTransactionService(
                providerRepository, userRepository, profileRepository, pendingRepository, challengeRepository,
                refreshTokenIssuer, jwtService, jwtProperties, googleProperties, challengeSecurity, clock);
    }

    @Test
    void linkedProviderLogsInBySubjectWithoutLookingUpGoogleEmail() {
        User user = user(10L, "old-email@example.com");
        UserProfile profile = new UserProfile(user);
        profile.setUsername("google_user");
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        UserAuthProvider link = new UserAuthProvider(user, AuthProvider.GOOGLE, "google-sub", "old-email@example.com", true);
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "google-sub"))
                .thenReturn(Optional.of(link));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        mockSession(user);

        GoogleAuthResponse response = service.authenticate(identity("google-sub", "new-email@example.com"), null, null, null);

        assertThat(response.user().id()).isEqualTo(10L);
        assertThat(response.profileCompleted()).isTrue();
        assertThat(response.nextStep()).isEqualTo("HOME");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void linkedProviderRejectsBlockedUserBeforeIssuingTokens() {
        User user = user(11L, "blocked@example.com");
        user.setStatus(UserStatus.BLOCKED);
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "blocked-sub"))
                .thenReturn(Optional.of(new UserAuthProvider(user, AuthProvider.GOOGLE, "blocked-sub", "blocked@example.com", true)));

        assertThatThrownBy(() -> service.authenticate(identity("blocked-sub", "blocked@example.com"), null, null, null))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.USER_BLOCKED);
        verify(refreshTokenIssuer, never()).issue(any(), any(), any(), any());
    }

    @Test
    void newGoogleIdentityCreatesSocialOnlyUserProfileProviderAndSession() {
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "new-sub"))
                .thenReturn(Optional.empty());
        when(pendingRepository.findByActiveIdentifierKeyForUpdate("EMAIL:new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 20L);
            return user;
        });
        when(profileRepository.saveAndFlush(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.findById(20L)).thenAnswer(invocation -> Optional.of(new UserProfile(user(20L, "new@example.com"))));
        when(providerRepository.saveAndFlush(any(UserAuthProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenIssuer.issue(any(), any(), any(), any())).thenReturn(new IssuedRefreshToken("system-refresh", 2_592_000));
        when(jwtService.generateAccessToken(20L, UserRole.USER.name())).thenReturn("system-access");

        GoogleAuthResponse response = service.authenticate(identity("new-sub", " New@Example.COM "), "d1", "Chrome", "203.0.113.1");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isNull();
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new@example.com");
        assertThat(userCaptor.getValue().getEmailVerifiedAt()).isEqualTo(LocalDateTime.now(clock));
        ArgumentCaptor<UserAuthProvider> providerCaptor = ArgumentCaptor.forClass(UserAuthProvider.class);
        verify(providerRepository).saveAndFlush(providerCaptor.capture());
        assertThat(providerCaptor.getValue().getProviderUserId()).isEqualTo("new-sub");
        assertThat(response.profileCompleted()).isFalse();
        assertThat(response.nextStep()).isEqualTo("COMPLETE_PROFILE");
        assertThat(response.accessToken()).isEqualTo("system-access");
    }

    @Test
    void activeEmailConflictCreatesChallengeWithoutLoginOrLink() {
        User existing = user(30L, "local@example.com");
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "conflict-sub"))
                .thenReturn(Optional.empty());
        when(pendingRepository.findByActiveIdentifierKeyForUpdate("EMAIL:local@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(existing));
        when(challengeSecurity.issue("conflict-sub")).thenReturn(
                new SocialChallengeSecurity.IssuedChallenge("raw-challenge", "a".repeat(64), "b".repeat(64)));

        assertThatThrownBy(() -> service.authenticate(identity("conflict-sub", "local@example.com"), null, null, null))
                .isInstanceOf(SocialConflictException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT);
        verify(challengeRepository).saveAndFlush(any(SocialAuthChallenge.class));
        verify(providerRepository, never()).saveAndFlush(any(UserAuthProvider.class));
        verify(refreshTokenIssuer, never()).issue(any(), any(), any(), any());
    }

    @Test
    void pendingEmailIsNotModifiedOrConvergedInPhase7A() {
        LocalDateTime now = LocalDateTime.now(clock);
        PendingRegistration pending = PendingRegistration.start(
                RegistrationType.EMAIL, "pending@example.com", "bcrypt", "f".repeat(64), "o".repeat(64),
                now.plusMinutes(10), now.plusMinutes(1), now.plusHours(24));
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "pending-sub"))
                .thenReturn(Optional.empty());
        when(pendingRepository.findByActiveIdentifierKeyForUpdate("EMAIL:pending@example.com"))
                .thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.authenticate(identity("pending-sub", "pending@example.com"), null, null, null))
                .isInstanceOf(BusinessException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_SOCIAL_PENDING_CONFLICT);
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.PENDING);
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    private void mockSession(User user) {
        when(refreshTokenIssuer.issue(any(), any(), any(), any())).thenReturn(new IssuedRefreshToken("refresh", 2_592_000));
        when(jwtService.generateAccessToken(user.getId(), user.getRole().name())).thenReturn("access");
    }

    private User user(Long id, String email) {
        User user = new User(email, null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private VerifiedGoogleIdentity identity(String subject, String email) {
        return new VerifiedGoogleIdentity(subject, email, true, "Student", null, "https://accounts.google.com");
    }
}
