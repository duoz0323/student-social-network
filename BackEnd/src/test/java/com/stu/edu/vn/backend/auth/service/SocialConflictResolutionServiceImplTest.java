package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictRequest;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
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
    void pendingPhoneContinueSocialDoesNotCopyPhoneOrPassword() {
        PendingRegistration pending = pending(RegistrationType.PHONE, "+84901234567");
        SocialAuthChallenge challenge = challenge(pending, "social@example.com");
        when(tokenHashService.sha256Hex("raw-token")).thenReturn("token-hash");
        when(challengeRepository.findByConflictTokenHashForUpdate("token-hash")).thenReturn(Optional.of(challenge));
        when(pendingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pending));
        when(providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, "subject"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 20L);
            return user;
        });
        when(refreshTokenIssuer.issue(any(), any(), any(), any()))
                .thenReturn(new IssuedRefreshToken("refresh", 3600));
        when(jwtService.generateAccessToken(20L, "USER")).thenReturn("access");
        when(jwtProperties.getAccessTokenExpirationMillis()).thenReturn(900_000L);

        var response = service.resolve("raw-token", new ResolveSocialConflictRequest(
                SocialResolutionAction.CANCEL_PENDING_AND_CONTINUE_SOCIAL, null, null), "127.0.0.1");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertThat(userCaptor.getValue().getPhoneNumber()).isNull();
        assertThat(userCaptor.getValue().getPasswordHash()).isNull();
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.CANCELLED);
        assertThat(response.accessToken()).isEqualTo("access");
    }

    @Test
    void activeEmailConflictCannotLinkThroughResolveEndpoint() {
        User existing = new User("existing@example.com", null, "hash");
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

    private PendingRegistration pending(RegistrationType type, String identifier) {
        LocalDateTime now = LocalDateTime.now(clock);
        PendingRegistration pending = PendingRegistration.start(type, identifier, "password-hash", "flow-hash",
                "otp-hash", now.plusMinutes(10), now.plusMinutes(1), now.plusHours(24));
        ReflectionTestUtils.setField(pending, "id", 10L);
        return pending;
    }

    private SocialAuthChallenge challenge(PendingRegistration pending, String socialEmail) {
        return SocialAuthChallenge.start("token-hash", AuthProvider.GOOGLE, "subject", "fingerprint",
                socialEmail, true, pending.getRegistrationType() == RegistrationType.PHONE
                        ? SocialConflictType.PENDING_PHONE_REQUIRES_CANCEL
                        : SocialConflictType.PENDING_EMAIL_MISMATCH,
                pending, null, LocalDateTime.now(clock).plusMinutes(5));
    }
}
