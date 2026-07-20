package com.stu.edu.vn.backend.auth.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class AuthGeneratorTest {

    @Test
    void otpContainsExactlySixDigits() {
        assertThat(new SecureRandomOtpGenerator(new SecureRandom(), 6).generate()).matches("[0-9]{6}");
    }

    @Test
    void flowTokenIsUrlSafeAndContainsConfiguredEntropy() {
        String token = new SecureFlowTokenGenerator(new SecureRandom(), 32).generate();
        assertThat(token).matches("[A-Za-z0-9_-]+");
        assertThat(Base64.getUrlDecoder().decode(token)).hasSize(32);
    }
}
