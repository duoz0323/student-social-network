package com.stu.edu.vn.backend.auth.generator;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Sinh flow token URL-safe bằng SecureRandom với entropy lấy từ cấu hình. */
@Component
public class SecureFlowTokenGenerator implements FlowTokenGenerator {

    private final SecureRandom secureRandom;
    private final int randomByteLength;

    @Autowired
    public SecureFlowTokenGenerator(AuthRegistrationProperties properties) {
        this(new SecureRandom(), properties.getFlowTokenRandomBytes());
    }

    SecureFlowTokenGenerator(SecureRandom secureRandom, int randomByteLength) {
        if (randomByteLength < 16) {
            throw new IllegalArgumentException("Flow token phải có ít nhất 16 byte ngẫu nhiên");
        }
        this.secureRandom = secureRandom;
        this.randomByteLength = randomByteLength;
    }

    @Override
    public String generate() {
        byte[] randomBytes = new byte[randomByteLength];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
