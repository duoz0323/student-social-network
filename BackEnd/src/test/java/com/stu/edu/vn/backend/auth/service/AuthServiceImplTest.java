package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.TokenHashService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AuthServiceImplTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = org.mockito.Mockito.mock(RefreshTokenRepository.class);
    private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    private final JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final JwtProperties jwtProperties = new JwtProperties();
    private final TokenHashService tokenHashService = new TokenHashService();
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
                currentUserProvider,
                clock
        );
    }

    @Test
    void registerCreatesUserProfileAndRefreshTokenHash() {
        RegisterRequest request = new RegisterRequest(" Student@Example.COM ", "Password@1", "Password@1");
        when(passwordEncoder.encode("Password@1")).thenReturn("bcrypt-hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 10L);
            return user;
        });
        when(userProfileRepository.saveAndFlush(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(10L, UserRole.USER.name())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(10L)).thenReturn("raw-refresh-token");

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        verify(userProfileRepository).saveAndFlush(profileCaptor.capture());
        verify(refreshTokenRepository).saveAndFlush(refreshTokenCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo("student@example.com");
        assertThat(userCaptor.getValue().getPhoneNumber()).isNull();
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(profileCaptor.getValue().getUser()).isSameAs(userCaptor.getValue());
        assertThat(profileCaptor.getValue().getProfileCompletedAt()).isNull();
        assertThat(refreshTokenCaptor.getValue().getTokenHash()).hasSize(64);
        assertThat(refreshTokenCaptor.getValue().getTokenHash()).isNotEqualTo("raw-refresh-token");
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.profileCompleted()).isFalse();
        assertThat(response.nextStep()).isEqualTo(RegisterResponse.ONBOARDING_PROFILE);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");
        assertThat(response.accessTokenExpiresIn()).isEqualTo(900);
    }

    @Test
    void registerRejectsDuplicateEmailBeforeSaving() {
        RegisterRequest request = new RegisterRequest("student@example.com", "Password@1", "Password@1");
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void registerMapsDatabaseRaceToDuplicatePhoneError() {
        RegisterRequest request = new RegisterRequest("091 234-5678", "Password@1", "Password@1");
        when(passwordEncoder.encode("Password@1")).thenReturn("bcrypt-hash");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate phone"));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }

    @Test
    void registerStopsWhenProfileCreationFailsSoTransactionCanRollback() {
        RegisterRequest request = new RegisterRequest("student@example.com", "Password@1", "Password@1");
        when(passwordEncoder.encode("Password@1")).thenReturn("bcrypt-hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 11L);
            return user;
        });
        when(userProfileRepository.saveAndFlush(any(UserProfile.class)))
                .thenThrow(new DataIntegrityViolationException("profile failed"));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REGISTER_FAILED);

        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void loginWithEmailCreatesSessionAndReturnsProfileState() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 20L);
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
        assertThat(response.user().id()).isEqualTo(20L);
        assertThat(response.user().role()).isEqualTo(UserRole.USER);
        assertThat(refreshTokenCaptor.getValue().getTokenHash()).hasSize(64);
        assertThat(refreshTokenCaptor.getValue().getTokenHash()).isNotEqualTo(response.refreshToken());
        assertThat(refreshTokenCaptor.getValue().getDeviceId()).isEqualTo("device-1");
        assertThat(refreshTokenCaptor.getValue().getDeviceInfo()).isEqualTo("Chrome on Windows");
        assertThat(refreshTokenCaptor.getValue().getIpAddress()).isEqualTo("203.0.113.10");
    }

    @Test
    void loginWithPhoneUsesNormalizedPhoneNumber() {
        User user = new User(null, "0912345678", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 21L);
        UserProfile profile = new UserProfile(user);
        profile.setProfileCompletedAt(java.time.LocalDateTime.now(clock));
        when(userRepository.findByPhoneNumber("0912345678")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "bcrypt-hash")).thenReturn(true);
        when(userProfileRepository.findById(21L)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(21L, UserRole.USER.name())).thenReturn("access-token");
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = authService.login(
                new LoginRequest("091 234-5678", "Password@1", null, null),
                null
        );

        verify(userRepository).findByPhoneNumber("0912345678");
        assertThat(response.profileCompleted()).isTrue();
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

        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 22L);
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
    void loginRejectsBlockedUserAfterPasswordMatches() {
        User user = new User("blocked@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 23L);
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findByEmail("blocked@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "bcrypt-hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("blocked@example.com", "Password@1", null, null),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_BLOCKED);

        verify(userProfileRepository, never()).findById(23L);
        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void refreshAccessTokenReturnsNewAccessTokenWithoutRotatingRefreshToken() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 30L);
        UserProfile profile = new UserProfile(user);
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        String rawRefreshToken = "raw-refresh-token";
        String tokenHash = tokenHashService.sha256Hex(rawRefreshToken);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(30L)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(30L)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(30L, UserRole.USER.name())).thenReturn("new-access-token");

        RefreshTokenResponse response = authService.refreshAccessToken(new RefreshTokenRequest(rawRefreshToken));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.tokenType()).isEqualTo(RefreshTokenResponse.BEARER_TOKEN_TYPE);
        assertThat(response.accessTokenExpiresIn()).isEqualTo(900);
        assertThat(response.profileCompleted()).isTrue();
        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void refreshAccessTokenRejectsMissingTokenHash() {
        String tokenHash = tokenHashService.sha256Hex("missing-refresh-token");
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("missing-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshAccessTokenRejectsRevokedToken() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 31L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("revoked-refresh-token"),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        refreshToken.setRevokedAt(LocalDateTime.now(clock));
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("revoked-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);
    }

    @Test
    void refreshAccessTokenRejectsExpiredToken() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 32L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("expired-refresh-token"),
                LocalDateTime.now(clock)
        );
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("expired-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    void refreshAccessTokenRejectsDeletedUser() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 33L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("deleted-user-refresh-token"),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(33L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("deleted-user-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshAccessTokenRejectsBlockedUser() {
        User user = new User("blocked@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 34L);
        user.setStatus(UserStatus.BLOCKED);
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex("blocked-user-refresh-token"),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(34L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest("blocked-user-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_BLOCKED);
    }

    @Test
    void logoutRevokesCurrentUsersRefreshToken() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 40L);
        String rawRefreshToken = "logout-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(40L);
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LogoutResponse response = authService.logout(new LogoutRequest(rawRefreshToken));

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).saveAndFlush(refreshTokenCaptor.capture());
        assertThat(response.loggedOut()).isTrue();
        assertThat(refreshTokenCaptor.getValue().getRevokedAt()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void logoutIsIdempotentWhenTokenAlreadyRevoked() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 41L);
        String rawRefreshToken = "already-revoked-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        refreshToken.setRevokedAt(LocalDateTime.now(clock).minusMinutes(5));
        when(currentUserProvider.getCurrentUserId()).thenReturn(41L);
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));

        LogoutResponse response = authService.logout(new LogoutRequest(rawRefreshToken));

        assertThat(response.loggedOut()).isTrue();
        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void refreshAfterLogoutRejectsRevokedRefreshToken() {
        User user = new User("student@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 42L);
        String rawRefreshToken = "refresh-after-logout-token";
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(42L);
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout(new LogoutRequest(rawRefreshToken));

        assertThatThrownBy(() -> authService.refreshAccessToken(new RefreshTokenRequest(rawRefreshToken)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);
    }

    @Test
    void logoutRejectsRefreshTokenOwnedByAnotherUser() {
        User owner = new User("owner@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(owner, "id", 43L);
        String rawRefreshToken = "another-user-refresh-token";
        RefreshToken refreshToken = new RefreshToken(
                owner,
                tokenHashService.sha256Hex(rawRefreshToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(44L);
        when(refreshTokenRepository.findByTokenHash(refreshToken.getTokenHash())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.logout(new LogoutRequest(rawRefreshToken)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
    }
}
