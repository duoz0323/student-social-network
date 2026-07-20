package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.config.AuthCleanupProperties;
import com.stu.edu.vn.backend.auth.entity.AuthMethodLinkChallenge;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.repository.AuthMethodLinkChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.user.entity.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class AuthCleanupTransactionServiceTest {
    private final PendingRegistrationRepository pending = org.mockito.Mockito.mock(PendingRegistrationRepository.class);
    private final SocialAuthChallengeRepository social = org.mockito.Mockito.mock(SocialAuthChallengeRepository.class);
    private final AuthMethodLinkChallengeRepository links = org.mockito.Mockito.mock(AuthMethodLinkChallengeRepository.class);
    private final ReauthenticationChallengeRepository reauth = org.mockito.Mockito.mock(ReauthenticationChallengeRepository.class);
    private final RefreshTokenRepository refresh = org.mockito.Mockito.mock(RefreshTokenRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), ZoneOffset.UTC);
    private AuthCleanupTransactionService service;

    @BeforeEach
    void setUp() {
        AuthCleanupProperties properties = new AuthCleanupProperties();
        properties.setBatchSize(2);
        properties.setRetention(Duration.ofDays(7));
        service = new AuthCleanupTransactionService(pending, social, links, reauth, refresh, properties, clock);
    }

    @Test
    void expiresEachActiveChallengeTypeWithoutExternalCalls() {
        LocalDateTime now = LocalDateTime.now(clock);
        PendingRegistration registration = PendingRegistration.start(RegistrationType.EMAIL, "a@example.com",
                "hash", "f".repeat(64), "o".repeat(64), now.minusMinutes(2),
                now.minusMinutes(1), now.minusDays(1));
        User user = user(7L);
        SocialAuthChallenge socialChallenge = SocialAuthChallenge.start("c".repeat(64), AuthProvider.GOOGLE,
                "provider-id", "f".repeat(64), null, null,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER, null, user, now.minusMinutes(1));
        AuthMethodLinkChallenge linkChallenge = AuthMethodLinkChallenge.start(user,
                AuthMethodLinkPurpose.LINK_EMAIL, "b@example.com", "f".repeat(64), "o".repeat(64),
                now.minusMinutes(1),
                now.minusMinutes(1), now.minusMinutes(1));
        ReauthenticationChallenge reauthChallenge = ReauthenticationChallenge.start(user, "t".repeat(64),
                ReauthenticationProofMethod.LOCAL_PASSWORD, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.GOOGLE, now.minusMinutes(1));
        when(pending.findExpiryBatchForUpdate(any(), any(), any(Pageable.class))).thenReturn(List.of(registration));
        when(social.findExpiryBatchForUpdate(any(), any(), any(Pageable.class))).thenReturn(List.of(socialChallenge));
        when(links.findExpiryBatchForUpdate(any(), any(), any(Pageable.class))).thenReturn(List.of(linkChallenge));
        when(reauth.findExpiryBatchForUpdate(any(), any(), any(Pageable.class))).thenReturn(List.of(reauthChallenge));

        assertThat(service.expirePending()).isOne();
        assertThat(service.expireSocial()).isOne();
        assertThat(service.expireLinkChallenges()).isOne();
        assertThat(service.expireReauthentication()).isOne();
        assertThat(registration.getStatus()).isEqualTo(OtpChallengeStatus.EXPIRED);
        assertThat(socialChallenge.getStatus()).isEqualTo(SocialAuthChallengeStatus.EXPIRED);
        assertThat(linkChallenge.getStatus()).isEqualTo(OtpChallengeStatus.EXPIRED);
        assertThat(reauthChallenge.getStatus()).isEqualTo(ReauthenticationChallengeStatus.EXPIRED);
    }

    @Test
    void cleanupUsesConfiguredBatchAndNeverDeletesCompletedPending() {
        when(pending.findCleanupBatchForUpdate(any(), any(), any(Pageable.class))).thenReturn(List.of());
        assertThat(service.cleanupPending()).isZero();
        verify(pending).findCleanupBatchForUpdate(
                org.mockito.ArgumentMatchers.argThat(statuses -> !statuses.contains(OtpChallengeStatus.COMPLETED)),
                any(), org.mockito.ArgumentMatchers.argThat(page -> page.getPageSize() == 2));
        verify(pending).deleteAllInBatch(List.of());
    }

    @Test
    void deletesOnlyExpiredRefreshTokensReturnedByLockedQuery() {
        RefreshToken expired = new RefreshToken(user(9L), "h".repeat(64), LocalDateTime.now(clock).minusMinutes(1));
        when(refresh.findExpiredBatchForUpdate(any(), any(Pageable.class))).thenReturn(List.of(expired));

        assertThat(service.cleanupExpiredRefreshTokens()).isOne();
        verify(refresh).deleteAllInBatch(List.of(expired));
    }

    @Test
    void emptyBatchesAreIdempotent() {
        when(refresh.findExpiredBatchForUpdate(any(), any(Pageable.class))).thenReturn(List.of());
        assertThat(service.cleanupExpiredRefreshTokens()).isZero();
        assertThat(service.cleanupExpiredRefreshTokens()).isZero();
        verify(refresh, never()).save(any());
    }

    private User user(long id) {
        User user = new User("student" + id + "@example.com", null, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
