package com.stu.edu.vn.backend.auth.google;

import com.stu.edu.vn.backend.auth.config.GoogleAuthProperties;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.TokenHashService;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/** Sinh challenge token opaque và HMAC fingerprint tách biệt khỏi JWT/OTP secret. */
@Component
public class SocialChallengeSecurity {

    private final GoogleAuthProperties properties;
    private final TokenHashService tokenHashService;
    private final SecureRandom secureRandom = new SecureRandom();

    public SocialChallengeSecurity(GoogleAuthProperties properties, TokenHashService tokenHashService) {
        this.properties = properties;
        this.tokenHashService = tokenHashService;
    }

    public IssuedChallenge issue(String providerSubject) {
        int byteLength = properties.getConflictTokenRandomBytes();
        if (byteLength < 16) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_AUTHENTICATION_FAILED);
        }
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new IssuedChallenge(rawToken, tokenHashService.sha256Hex(rawToken), fingerprint(providerSubject));
    }

    private String fingerprint(String subject) {
        String secret = properties.getIdentityFingerprintSecret();
        if (secret == null || secret.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_AUTHENTICATION_FAILED);
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(subject.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new BusinessException(ErrorCode.AUTH_GOOGLE_AUTHENTICATION_FAILED);
        }
    }

    public record IssuedChallenge(String rawToken, String tokenHash, String identityFingerprint) { }
}
