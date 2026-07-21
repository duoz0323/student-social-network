package com.stu.edu.vn.backend.auth.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthHmacServiceTest {

    private AuthHmacService service;

    @BeforeEach
    void setUp() {
        AuthRegistrationProperties properties = new AuthRegistrationProperties();
        properties.setOtpHmacSecret("otp-secret-for-test-only");
        properties.setFlowTokenHmacSecret("flow-secret-for-test-only");
        service = new AuthHmacService(properties);
    }

    @Test
    void hashesAndVerifiesOtp() {
        String hash = service.hashOtp("123456");
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(service.verifyOtp("123456", hash)).isTrue();
        assertThat(service.verifyOtp("654321", hash)).isFalse();
    }

    @Test
    void flowTokenUsesItsOwnSecretAndWrongTokenDoesNotVerify() {
        String hash = service.hashFlowToken("opaque-token");
        assertThat(service.verifyFlowToken("opaque-token", hash)).isTrue();
        assertThat(service.verifyFlowToken("wrong-token", hash)).isFalse();
        assertThat(hash).isNotEqualTo(service.hashOtp("opaque-token"));
    }
}
