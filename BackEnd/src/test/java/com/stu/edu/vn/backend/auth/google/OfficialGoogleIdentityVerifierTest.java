package com.stu.edu.vn.backend.auth.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.stu.edu.vn.backend.auth.config.GoogleAuthProperties;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfficialGoogleIdentityVerifierTest {

    private final GoogleIdTokenVerifier sdkVerifier = org.mockito.Mockito.mock(GoogleIdTokenVerifier.class);
    private final GoogleAuthProperties properties = new GoogleAuthProperties();
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T01:00:00Z"), ZoneOffset.UTC);
    private OfficialGoogleIdentityVerifier verifier;

    @BeforeEach
    void setUp() {
        properties.setClientId("web-client-id");
        properties.setClockSkew(Duration.ofSeconds(30));
        verifier = new OfficialGoogleIdentityVerifier(sdkVerifier, properties, clock);
    }

    @Test
    void verifiedTokenReturnsInternalIdentity() throws Exception {
        GoogleIdToken verifiedToken = token(validPayload());
        when(sdkVerifier.verify("valid-token")).thenReturn(verifiedToken);

        VerifiedGoogleIdentity identity = verifier.verify("valid-token");

        assertThat(identity.subject()).isEqualTo("google-sub");
        assertThat(identity.email()).isEqualTo("student@example.com");
        assertThat(identity.emailVerified()).isTrue();
    }

    @Test
    void blankTokenIsRejectedWithoutCallingGoogleSdk() {
        assertError(() -> verifier.verify(" "), ErrorCode.AUTH_GOOGLE_TOKEN_REQUIRED);
    }

    @Test
    void invalidSignatureOrMalformedTokenIsRejected() throws Exception {
        when(sdkVerifier.verify("invalid-token")).thenReturn(null);
        assertError(() -> verifier.verify("invalid-token"), ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
    }

    @Test
    void wrongAudienceIsRejected() throws Exception {
        GoogleIdToken.Payload payload = validPayload().setAudience("other-client");
        GoogleIdToken verifiedToken = token(payload);
        when(sdkVerifier.verify("token")).thenReturn(verifiedToken);
        assertError(() -> verifier.verify("token"), ErrorCode.AUTH_GOOGLE_AUDIENCE_INVALID);
    }

    @Test
    void wrongIssuerIsRejected() throws Exception {
        GoogleIdToken.Payload payload = validPayload().setIssuer("https://attacker.example");
        GoogleIdToken verifiedToken = token(payload);
        when(sdkVerifier.verify("token")).thenReturn(verifiedToken);
        assertError(() -> verifier.verify("token"), ErrorCode.AUTH_GOOGLE_ISSUER_INVALID);
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        GoogleIdToken.Payload payload = validPayload().setExpirationTimeSeconds(Instant.now(clock).minusSeconds(31).getEpochSecond());
        GoogleIdToken verifiedToken = token(payload);
        when(sdkVerifier.verify("token")).thenReturn(verifiedToken);
        assertError(() -> verifier.verify("token"), ErrorCode.AUTH_GOOGLE_TOKEN_EXPIRED);
    }

    @Test
    void missingSubjectIsRejected() throws Exception {
        GoogleIdToken.Payload payload = validPayload().setSubject(" ");
        GoogleIdToken verifiedToken = token(payload);
        when(sdkVerifier.verify("token")).thenReturn(verifiedToken);
        assertError(() -> verifier.verify("token"), ErrorCode.AUTH_GOOGLE_SUBJECT_MISSING);
    }

    @Test
    void missingEmailIsRejected() throws Exception {
        GoogleIdToken.Payload payload = validPayload().setEmail(null);
        GoogleIdToken verifiedToken = token(payload);
        when(sdkVerifier.verify("token")).thenReturn(verifiedToken);
        assertError(() -> verifier.verify("token"), ErrorCode.AUTH_GOOGLE_EMAIL_MISSING);
    }

    @Test
    void unverifiedEmailIsRejected() throws Exception {
        GoogleIdToken.Payload payload = validPayload().setEmailVerified(false);
        GoogleIdToken verifiedToken = token(payload);
        when(sdkVerifier.verify("token")).thenReturn(verifiedToken);
        assertError(() -> verifier.verify("token"), ErrorCode.AUTH_GOOGLE_EMAIL_NOT_VERIFIED);
    }

    private GoogleIdToken.Payload validPayload() {
        return new GoogleIdToken.Payload()
                .setAudience("web-client-id")
                .setIssuer("https://accounts.google.com")
                .setExpirationTimeSeconds(Instant.now(clock).plusSeconds(300).getEpochSecond())
                .setSubject("google-sub")
                .setEmail("student@example.com")
                .setEmailVerified(true);
    }

    private GoogleIdToken token(GoogleIdToken.Payload payload) {
        GoogleIdToken token = org.mockito.Mockito.mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);
        return token;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode expected) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(expected);
    }
}
