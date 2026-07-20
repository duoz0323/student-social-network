package com.stu.edu.vn.backend.auth.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IdentifierMaskerTest {

    private final IdentifierMasker masker = new IdentifierMasker();

    @Test
    void masksEmail() {
        assertThat(masker.mask(new NormalizedIdentifier(IdentifierType.EMAIL, "student@example.com")))
                .isEqualTo("s***@example.com");
    }

    @Test
    void masksPhone() {
        assertThat(masker.mask(new NormalizedIdentifier(IdentifierType.PHONE_NUMBER, "0912345678")))
                .isEqualTo("******5678");
    }
}
