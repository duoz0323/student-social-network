package com.stu.edu.vn.backend.auth.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailMaskerTest {

    private final EmailMasker masker = new EmailMasker();

    @Test
    void masksEmail() {
        assertThat(masker.mask(new NormalizedEmail("student@example.com")))
                .isEqualTo("s***@example.com");
    }

}

