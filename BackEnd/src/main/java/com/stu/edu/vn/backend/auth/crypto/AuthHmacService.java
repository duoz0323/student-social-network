package com.stu.edu.vn.backend.auth.crypto;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Băm OTP và flow token bằng hai HMAC secret độc lập, hoàn toàn tách khỏi JWT secret.
 */
@Component
public class AuthHmacService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String otpSecret;
    private final String flowTokenSecret;

    public AuthHmacService(AuthRegistrationProperties properties) {
        this.otpSecret = properties.getOtpHmacSecret();
        this.flowTokenSecret = properties.getFlowTokenHmacSecret();
    }

    public String hashOtp(String rawOtp) {
        return hmacHex(rawOtp, otpSecret);
    }

    public boolean verifyOtp(String rawOtp, String expectedHash) {
        return verify(rawOtp, expectedHash, otpSecret);
    }

    public String hashFlowToken(String rawToken) {
        return hmacHex(rawToken, flowTokenSecret);
    }

    public boolean verifyFlowToken(String rawToken, String expectedHash) {
        return verify(rawToken, expectedHash, flowTokenSecret);
    }

    private boolean verify(String rawValue, String expectedHash, String secret) {
        if (rawValue == null || expectedHash == null) {
            return false;
        }
        byte[] actual = hmacHex(rawValue, secret).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }

    private String hmacHex(String rawValue, String secret) {
        if (rawValue == null || secret == null || secret.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_START_FAILED);
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(rawValue.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_START_FAILED);
        }
    }
}
