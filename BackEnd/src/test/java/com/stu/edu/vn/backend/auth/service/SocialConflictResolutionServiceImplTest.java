package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictRequest;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.TokenHashService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SocialConflictResolutionServiceImplTest {
    @Mock SocialAuthChallengeRepository challengeRepository;
    @Mock PendingRegistrationRepository pendingRepository;
    @Mock UserAuthProviderRepository providerRepository;
    @Mock UserRepository userRepository;
    @Mock UserProfileRepository profileRepository;
    @Mock RefreshTokenIssuer refreshTokenIssuer;
    @Mock JwtService jwtService;
    @Mock JwtProperties jwtProperties;
    @Mock TokenHashService tokenHashService;

    private SocialConflictResolutionServiceImpl service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-19T03:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new SocialConflictResolutionServiceImpl(challengeRepository, pendingRepository,
                providerRepository, userRepository, profileRepository, refreshTokenIssuer,
                jwtService, jwtProperties, tokenHashService, clock);
    }

    @Test
    void continueOtpKeepsPendingAndConsumesChallenge() {
        PendingRegistration pending = pending(RegistrationType.EMAIL, "local@example.com");
        SocialAuthChallenge challenge = challenge(pending, "social@example.com");
        when(tokenHashService.sha256Hex("raw-token")).thenReturn("token-hash");
        when(challengeRepository.findByConflictTokenHashForUpdate("token-hash")).thenReturn(Optional.of(challenge));
        when(pendingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pending));

        var response = service.resolve("raw-token",
                new ResolveSocialConflictRequest(SocialResolutionAction.CONTINUE_OTP, null, null), "127.0.0.1");

        assertThat(response.nextStep()).isEqualTo("VERIFY_OTP");
        assertThat(response.accessToken()).isNull();
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.PENDING);
        assertThat(challenge.getStatus()).isEqualTo(SocialAuthChallengeStatus.RESOLVED);
    }


    @Test
    void activeEmailConflictCannotLinkThroughResolveEndpoint() {
        User existing = new User("existing@example.com", "hash");
        SocialAuthChallenge challenge = SocialAuthChallenge.start("token-hash", AuthProvider.GOOGLE,
                "subject", "fingerprint", "existing@example.com", true,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER, null, existing,
                LocalDateTime.now(clock).plusMinutes(5));
        when(tokenHashService.sha256Hex("raw-token")).thenReturn("token-hash");
        when(challengeRepository.findByConflictTokenHashForUpdate("token-hash")).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.resolve("raw-token", new ResolveSocialConflictRequest(
                SocialResolutionAction.LOGIN_EXISTING_ACCOUNT, null, null), "127.0.0.1"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_SOCIAL_CHALLENGE_ACTION_INVALID));
    }

    @Test
    void facebookActiveEmailConflictCanCreateIndependentUserWithoutEmailOrInheritedAdminRole() {
        User existingAdmin = new User("admin@example.com", "hash");
        existingAdmin.setRole(UserRole.ADMIN);
        ReflectionTestUtils.setField(existingAdmin, "id", 1L);
        SocialAuthChallenge challenge = SocialAuthChallenge.start("token-hash", AuthProvider.FACEBOOK,
                "facebook-user-id", "fingerprint", "admin@example.com", true,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER, null, existingAdmin,
                LocalDateTime.now(clock).plusMinutes(5));
        when(tokenHashService.sha256Hex("raw-token")).thenReturn("token-hash");
        when(challengeRepository.findByConflictTokenHashForUpdate("token-hash")).thenReturn(Optional.of(challenge));
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(
                AuthProvider.FACEBOOK, "facebook-user-id")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 50L);
            return user;
        });
        when(refreshTokenIssuer.issue(any(), any(), any(), any()))
                .thenReturn(new IssuedRefreshToken("refresh-token", 2_592_000));
        when(jwtProperties.getAccessTokenExpirationMillis()).thenReturn(900_000L);
        when(jwtService.generateAccessToken(50L, "USER")).thenReturn("access-token");

        var response = service.resolve("raw-token", new ResolveSocialConflictRequest(
                SocialResolutionAction.CONTINUE_WITH_SEPARATE_ACCOUNT, "device-1", "Chrome"), "127.0.0.1");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserAuthProvider> providerCaptor = ArgumentCaptor.forClass(UserAuthProvider.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        verify(providerRepository).saveAndFlush(providerCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isNull();
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.USER);
        assertThat(providerCaptor.getValue().getProviderEmail()).isEqualTo("admin@example.com");
        assertThat(providerCaptor.getValue().getUser().getId()).isEqualTo(50L);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.nextStep()).isEqualTo("COMPLETE_PROFILE");
        assertThat(response.user().role()).isEqualTo(UserRole.USER);
        assertThat(challenge.getResolvedUser().getId()).isEqualTo(50L);
        assertThat(challenge.getConflictingUser().getId()).isEqualTo(1L);
        assertThat(challenge.getStatus()).isEqualTo(SocialAuthChallengeStatus.RESOLVED);
    }

    @Test
    void googleActiveEmailConflictCannotUseFacebookOnlySeparateAccountAction() {
        User existing = new User("existing@example.com", "hash");
        SocialAuthChallenge challenge = SocialAuthChallenge.start("token-hash", AuthProvider.GOOGLE,
                "google-sub", "fingerprint", "existing@example.com", true,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER, null, existing,
                LocalDateTime.now(clock).plusMinutes(5));
        when(tokenHashService.sha256Hex("raw-token")).thenReturn("token-hash");
        when(challengeRepository.findByConflictTokenHashForUpdate("token-hash")).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.resolve("raw-token", new ResolveSocialConflictRequest(
                SocialResolutionAction.CONTINUE_WITH_SEPARATE_ACCOUNT, null, null), "127.0.0.1"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_SOCIAL_CHALLENGE_ACTION_INVALID));
    }

    private PendingRegistration pending(RegistrationType type, String identifier) {
        LocalDateTime now = LocalDateTime.now(clock);
        PendingRegistration pending = PendingRegistration.start(type, identifier, "password-hash", "flow-hash",
                "otp-hash", now.plusMinutes(10), now.plusMinutes(1), now.plusHours(24));
        ReflectionTestUtils.setField(pending, "id", 10L);
        return pending;
    }

    private SocialAuthChallenge challenge(PendingRegistration pending, String socialEmail) {
        return SocialAuthChallenge.start("token-hash", AuthProvider.GOOGLE, "subject", "fingerprint",
                socialEmail, true, pending.getRegistrationType() == RegistrationType.EMAIL
                        ? SocialConflictType.PENDING_EMAIL_MISMATCH
                        : SocialConflictType.PENDING_EMAIL_MISMATCH,
                pending, null, LocalDateTime.now(clock).plusMinutes(5));
    }
}

