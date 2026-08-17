package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.mapper.AuthMapper;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.TokenHashService;
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
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AuthServiceImplTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = org.mockito.Mockito.mock(RefreshTokenRepository.class);
    private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    private final JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    private final JwtProperties jwtProperties = new JwtProperties();
    private final TokenHashService tokenHashService = new TokenHashService();
    private final AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-21T10:00:00Z"), ZoneOffset.UTC);

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        jwtProperties.setAccessTokenExpirationMillis(900_000);
        jwtProperties.setRefreshTokenExpirationMillis(2_592_000_000L);
        authService = new AuthServiceImpl(
                userRepository,
                userProfileRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                jwtProperties,
                tokenHashService,
                authMapper,
                clock,
                new RefreshTokenIssuer(
                        refreshTokenRepository,
                        tokenHashService,
                        jwtProperties,
                        clock
                )
        );
    }

    @Test
    void loginWithEmailCreatesSessionAndReturnsProfileState() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 20L);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        UserProfile profile = new UserProfile(user);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "bcrypt-hash")).thenReturn(true);
        when(userProfileRepository.findById(20L)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(20L, UserRole.USER.name())).thenReturn("access-token");
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = authService.login(
                new LoginRequest(" Student@Example.COM ", "Password@1", "device-1", "Chrome on Windows"),
                "203.0.113.10"
        );

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).saveAndFlush(refreshTokenCaptor.capture());

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.refreshToken()).doesNotContain(".");
        assertThat(response.tokenType()).isEqualTo(LoginResponse.BEARER_TOKEN_TYPE);
        assertThat(response.accessTokenExpiresIn()).isEqualTo(900);
        assertThat(response.refreshTokenExpiresIn()).isEqualTo(2_592_000);
        assertThat(response.profileCompleted()).isFalse();
        assertThat(response.nextStep()).isEqualTo(LoginResponse.NEXT_STEP_COMPLETE_PROFILE);
        assertThat(response.user().id()).isEqualTo(20L);
        assertThat(response.user().role()).isEqualTo(UserRole.USER);
        assertThat(refreshTokenCaptor.getValue().getTokenHash()).hasSize(64);
        assertThat(refreshTokenCaptor.getValue().getTokenHash()).isNotEqualTo(response.refreshToken());
        assertThat(refreshTokenCaptor.getValue().getDeviceId()).isEqualTo("device-1");
        assertThat(refreshTokenCaptor.getValue().getDeviceInfo()).isEqualTo("Chrome on Windows");
        assertThat(refreshTokenCaptor.getValue().getIpAddress()).isEqualTo("203.0.113.10");
    }


    @Test
    void loginRejectsMissingUserAndWrongPasswordWithSameErrorCode() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("missing@example.com", "Password@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 22L);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword@1", "bcrypt-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("student@example.com", "WrongPassword@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void loginRejectsBlockedUserBeforePasswordAndTokenCreation() {
        User user = new User("blocked@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 23L);
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findByEmail("blocked@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("blocked@example.com", "Password@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_BLOCKED);

        verify(userProfileRepository, never()).findById(23L);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void loginRejectsUnverifiedEmailBeforePasswordAndTokenCreation() {
        User user = new User("unverified@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 24L);
        when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("unverified@example.com", "Password@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_IDENTIFIER_NOT_VERIFIED);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }


    @Test
    void loginRejectsSocialOnlyUserWithoutCallingPasswordEncoder() {
        User user = new User("social@example.com", null);
        ReflectionTestUtils.setField(user, "id", 26L);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("social@example.com", "Password@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_PASSWORD_LOGIN_NOT_AVAILABLE);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void loginDoesNotGenerateAccessTokenWhenRefreshTokenCreationFails() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 27L);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        UserProfile profile = new UserProfile(user);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "bcrypt-hash")).thenReturn(true);
        when(userProfileRepository.findById(27L)).thenReturn(Optional.of(profile));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("test failure"));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("student@example.com", "Password@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_TOKEN_CREATION_FAILED);

        verify(jwtService, never()).generateAccessToken(27L, UserRole.USER.name());
    }

    @Test
    void loginFailsAsOneTransactionWhenAccessTokenCreationFails() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 28L);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        UserProfile profile = new UserProfile(user);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "bcrypt-hash")).thenReturn(true);
        when(userProfileRepository.findById(28L)).thenReturn(Optional.of(profile));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(28L, UserRole.USER.name()))
                .thenThrow(new IllegalStateException("test failure"));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("student@example.com", "Password@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_LOGIN_FAILED);

        verify(refreshTokenRepository).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void concurrentLoginsCreateIndependentRefreshTokenSessions() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 29L);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        UserProfile profile = new UserProfile(user);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "bcrypt-hash")).thenReturn(true);
        when(userProfileRepository.findById(29L)).thenReturn(Optional.of(profile));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(29L, UserRole.USER.name())).thenReturn("access-token");
        LoginRequest request = new LoginRequest("student@example.com", "Password@1", null, null);

        CompletableFuture<LoginResponse> first = CompletableFuture.supplyAsync(
                () -> authService.login(request, "203.0.113.10")
        );
        CompletableFuture<LoginResponse> second = CompletableFuture.supplyAsync(
                () -> authService.login(request, "203.0.113.11")
        );

        assertThat(first.join().refreshToken()).isNotEqualTo(second.join().refreshToken());
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void refreshAccessTokenRotatesRefreshTokenAndRevokesOldSession() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 30L);
        UserProfile profile = new UserProfile(user);
        profile.setUsername("student_30");
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        String rawRefreshToken = "raw-refresh-token";
        String tokenHash = tokenHashService.sha256Hex(rawRefreshToken);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(tokenHash)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(30L)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(30L)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(30L, UserRole.USER.name())).thenReturn("new-access-token");
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenResponse response = authService.refreshAccessToken(new RefreshTokenRequest(rawRefreshToken));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isNotBlank().isNotEqualTo(rawRefreshToken);
        assertThat(response.tokenType()).isEqualTo(RefreshTokenResponse.BEARER_TOKEN_TYPE);
        assertThat(response.accessTokenExpiresIn()).isEqualTo(900);
        assertThat(response.refreshTokenExpiresIn()).isEqualTo(2_592_000);
        assertThat(response.profileCompleted()).isTrue();
        assertThat(refreshToken.getRevokedAt()).isEqualTo(LocalDateTime.now(clock));
        verify(refreshTokenRepository).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void refreshAccessTokenRejectsMissingTokenHash() {
        String tokenHash = tokenHashService.sha256Hex("missing-refresh-token");
        when(refreshTokenRepository.findByTokenHashForUpdate(tokenHash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("missing-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshAccessTokenRejectsRevokedToken() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 31L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("revoked-refresh-token"),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        refreshToken.revoke(LocalDateTime.now(clock));
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("revoked-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);
    }

    @Test
    void refreshAccessTokenRejectsExpiredToken() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 32L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("expired-refresh-token"),
                LocalDateTime.now(clock)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("expired-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    void refreshAccessTokenRejectsDeletedUser() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 33L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("deleted-user-refresh-token"),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(33L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("deleted-user-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshAccessTokenRejectsBlockedUser() {
        User user = new User("blocked@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 34L);
        user.setStatus(UserStatus.BLOCKED);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("blocked-user-refresh-token"),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(34L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("blocked-user-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_BLOCKED);
    }

    @Test
    void logoutRevokesCurrentUsersRefreshToken() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 40L);
        String rawRefreshToken = "logout-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LogoutResponse response = authService.logout(new LogoutRequest(rawRefreshToken));

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).saveAndFlush(refreshTokenCaptor.capture());
        assertThat(response.loggedOut()).isTrue();
        assertThat(refreshTokenCaptor.getValue().getRevokedAt()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void logoutIsIdempotentWhenTokenAlreadyRevoked() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 41L);
        String rawRefreshToken = "already-revoked-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        refreshToken.revoke(LocalDateTime.now(clock).minusMinutes(5));
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));

        LogoutResponse response = authService.logout(new LogoutRequest(rawRefreshToken));

        assertThat(response.loggedOut()).isTrue();
        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void refreshAfterLogoutRejectsRevokedRefreshToken() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 42L);
        String rawRefreshToken = "refresh-after-logout-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout(new LogoutRequest(rawRefreshToken));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest(rawRefreshToken)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);
    }

    @Test
    void logoutUsesTokenOwnershipWithoutDependingOnAccessToken() {
        User owner = new User("owner@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(owner, "id", 43L);
        String rawRefreshToken = "another-user-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                owner,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LogoutResponse response = authService.logout(new LogoutRequest(rawRefreshToken));

        assertThat(response.loggedOut()).isTrue();
        assertThat(refreshToken.getRevokedAt()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void refreshMapsJwtFailureToRotationError() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 45L);
        UserProfile profile = new UserProfile(user);
        String rawRefreshToken = "jwt-failure-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash()))
                .thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(45L)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(45L)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(45L, UserRole.USER.name()))
                .thenThrow(new IllegalStateException("JWT generation failed"));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest(rawRefreshToken)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REFRESH_TOKEN_ROTATION_FAILED);

        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void logoutExpiredTokenIsIdempotentlyRevoked() {
        User user = new User("student@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 46L);
        String rawRefreshToken = "expired-logout-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).minusSeconds(1)
        );
        when(refreshTokenRepository.findByTokenHashForUpdate(refreshToken.getTokenHash()))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(authService.logout(new LogoutRequest(rawRefreshToken)).loggedOut()).isTrue();
        assertThat(refreshToken.getRevokedAt()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void logoutRejectsUnknownTokenWithoutLeakingLookupDetails() {
        String rawRefreshToken = "unknown-logout-refresh-token";
        when(refreshTokenRepository.findByTokenHashForUpdate(tokenHashService.sha256Hex(rawRefreshToken)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(new LogoutRequest(rawRefreshToken)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}

