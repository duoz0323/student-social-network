package com.stu.edu.vn.backend.auth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.stu.edu.vn.backend.auth.config.GoogleAuthProperties;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import org.springframework.stereotype.Component;

/** Adapter duy nhất phụ thuộc Google SDK; không ghi raw ID Token vào log hoặc exception. */
@Component
public class OfficialGoogleIdentityVerifier implements GoogleIdentityVerifier {

    private final GoogleIdTokenVerifier verifier;
    private final GoogleAuthProperties properties;
    private final Clock clock;

    public OfficialGoogleIdentityVerifier(GoogleIdTokenVerifier verifier, GoogleAuthProperties properties, Clock clock) {
        this.verifier = verifier;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public VerifiedGoogleIdentity verify(String rawIdToken) {
        if (rawIdToken == null || rawIdToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_TOKEN_REQUIRED);
        }
        try {
            GoogleIdToken token = verifier.verify(rawIdToken);
            if (token == null) {
                throw new BusinessException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
            }
            return toVerifiedIdentity(token.getPayload());
        } catch (IOException | GeneralSecurityException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
        }
    }

    private VerifiedGoogleIdentity toVerifiedIdentity(GoogleIdToken.Payload payload) {
        if (!containsAudience(payload.getAudienceAsList(), properties.getClientId())) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_AUDIENCE_INVALID);
        }
        if (!properties.getIssuers().contains(payload.getIssuer())) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_ISSUER_INVALID);
        }
        Long expiration = payload.getExpirationTimeSeconds();
        if (expiration == null || expiration + properties.getClockSkew().toSeconds() < Instant.now(clock).getEpochSecond()) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_TOKEN_EXPIRED);
        }
        String subject = requireText(payload.getSubject(), ErrorCode.AUTH_GOOGLE_SUBJECT_MISSING);
        // Google subject vẫn là identity chính; email thiếu/chưa verified chỉ không được dùng làm local email.
        String email = optionalText(payload.getEmail());
        boolean emailVerified = email != null && Boolean.TRUE.equals(payload.getEmailVerified());
        return new VerifiedGoogleIdentity(
                subject, email, emailVerified,
                stringClaim(payload, "name"), stringClaim(payload, "picture"), payload.getIssuer()
        );
    }

    private boolean containsAudience(Collection<String> audiences, String clientId) {
        return audiences != null && clientId != null && audiences.contains(clientId);
    }

    private String requireText(String value, ErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(errorCode);
        }
        return value;
    }

    private String stringClaim(GoogleIdToken.Payload payload, String name) {
        Object value = payload.get(name);
        return value instanceof String text && !text.isBlank() ? text : null;
    }

    private String optionalText(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
