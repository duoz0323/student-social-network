package com.stu.edu.vn.backend.auth.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import com.stu.edu.vn.backend.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthChallengeDomainTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 19, 12, 0);

    @Test
    void pendingResendRotatesSecretsButKeepsPasswordAndPendingExpiry() {
        PendingRegistration registration = pendingRegistration();
        LocalDateTime originalExpiry = registration.getExpiresAt();
        String originalPasswordHash = registration.getPasswordHash();

        registration.markDeliveryFailed(NOW.plusSeconds(1), "SMTP_REJECTED");
        registration.recordFailedAttempt();
        registration.resend("otp-hash-v2", NOW.plusMinutes(11), NOW.plusMinutes(2));

        assertEquals(2, registration.getOtpVersion());
        assertEquals(1, registration.getResendCount());
        assertEquals(0, registration.getFailedAttempts());
        assertEquals(OtpDeliveryStatus.PENDING, registration.getDeliveryStatus());
        assertNull(registration.getDeliveryFailureCode());
        assertEquals(originalPasswordHash, registration.getPasswordHash());
        assertEquals("flow-hash-v1", registration.getFlowTokenHash());
        assertEquals(originalExpiry, registration.getExpiresAt());
    }

    @Test
    void pendingCompleteClearsSecretsAndKeepsIdentifierForAudit() {
        PendingRegistration registration = pendingRegistration();
        User user = persistedUser(10L);

        registration.complete(user, NOW.plusMinutes(5));

        assertEquals(OtpChallengeStatus.COMPLETED, registration.getStatus());
        assertEquals("student@example.com", registration.getIdentifierNormalized());
        assertSame(user, registration.getCompletedUser());
        assertNull(registration.getActiveIdentifierKey());
        assertNull(registration.getPasswordHash());
        assertEquals("flow-hash-v1", registration.getFlowTokenHash());
        assertNull(registration.getOtpHash());
        assertEquals(NOW.plusMinutes(5), registration.getTerminalAt());
    }

    @Test
    void pendingCancelRemovesIdentifierAndVerificationSecretsButKeepsLookupHash() {
        PendingRegistration registration = pendingRegistration();

        registration.cancel(NOW.plusMinutes(3));

        assertEquals(OtpChallengeStatus.CANCELLED, registration.getStatus());
        assertNull(registration.getIdentifierNormalized());
        assertNull(registration.getActiveIdentifierKey());
        assertNull(registration.getPasswordHash());
        assertEquals("flow-hash-v1", registration.getFlowTokenHash());
        assertNull(registration.getOtpHash());
    }

    @Test
    void linkCompleteClearsActiveKeysAndSecrets() {
        AuthMethodLinkChallenge challenge = AuthMethodLinkChallenge.start(
                persistedUser(20L),
                AuthMethodLinkPurpose.LINK_EMAIL,
                "linked@example.com",
                "flow-hash",
                "otp-hash",
                NOW.plusMinutes(10),
                NOW.plusMinutes(1),
                NOW.plusMinutes(15)
        );

        challenge.complete(NOW.plusMinutes(2));

        assertEquals(OtpChallengeStatus.COMPLETED, challenge.getStatus());
        assertNull(challenge.getIdentifierNormalized());
        assertNull(challenge.getActiveIdentifierKey());
        assertNull(challenge.getActiveUserPurposeKey());
        assertNull(challenge.getFlowTokenHash());
        assertNull(challenge.getOtpHash());
    }

    @Test
    void activeEmailSocialConflictCannotResolveAsAutomaticLink() {
        User existingUser = persistedUser(30L);
        SocialAuthChallenge challenge = SocialAuthChallenge.start(
                "conflict-hash",
                AuthProvider.GOOGLE,
                "google-sub",
                "identity-fingerprint",
                "student@example.com",
                true,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
                null,
                existingUser,
                NOW.plusMinutes(5)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> challenge.resolve(
                        SocialResolutionAction.CANCEL_PENDING_AND_CONTINUE_SOCIAL,
                        existingUser,
                        NOW.plusMinutes(1)
                )
        );

        challenge.resolve(SocialResolutionAction.LOGIN_EXISTING_ACCOUNT, null, NOW.plusMinutes(1));
        assertEquals(SocialAuthChallengeStatus.RESOLVED, challenge.getStatus());
        assertNull(challenge.getConflictTokenHash());
        assertNull(challenge.getProviderUserId());
        assertNull(challenge.getProviderEmail());
        assertNull(challenge.getActiveProviderKey());
        assertEquals("identity-fingerprint", challenge.getProviderIdentityFingerprint());
    }

    @Test
    void reauthenticationProofCanDifferFromTargetAndConsumeClearsToken() {
        ReauthenticationChallenge challenge = ReauthenticationChallenge.start(
                persistedUser(40L),
                "reauth-hash",
                ReauthenticationProofMethod.GOOGLE,
                ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.EMAIL,
                NOW.plusMinutes(5)
        );

        challenge.consume(NOW.plusMinutes(1));

        assertEquals(ReauthenticationChallengeStatus.CONSUMED, challenge.getStatus());
        assertNull(challenge.getTokenHash());
        assertNull(challenge.getActiveUserScopeKey());
        assertEquals(NOW.plusMinutes(1), challenge.getTerminalAt());
    }

    private PendingRegistration pendingRegistration() {
        return PendingRegistration.start(
                RegistrationType.EMAIL,
                "student@example.com",
                "bcrypt-hash",
                "flow-hash-v1",
                "otp-hash-v1",
                NOW.plusMinutes(10),
                NOW.plusMinutes(1),
                NOW.plusHours(24)
        );
    }

    private User persistedUser(Long id) {
        User user = new User("student" + id + "@example.com", null, "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
