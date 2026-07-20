package com.stu.edu.vn.backend.auth.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class IdentifierNormalizerTest {

    @Test
    void normalizesAndDetectsEmail() {
        NormalizedIdentifier result = IdentifierNormalizer.normalize(" Student@Example.COM ");

        assertThat(result.type()).isEqualTo(IdentifierType.EMAIL);
        assertThat(result.value()).isEqualTo("student@example.com");
    }

    @Test
    void normalizesAndDetectsPhone() {
        NormalizedIdentifier result = IdentifierNormalizer.normalize(" 091 234-5678 ");

        assertThat(result.type()).isEqualTo(IdentifierType.PHONE_NUMBER);
        assertThat(result.value()).isEqualTo("0912345678");
    }

    @Test
    void rejectsInvalidIdentifierAndDoesNotRemoveInternalEmailSpaces() {
        assertThatThrownBy(() -> IdentifierNormalizer.normalize("student @example.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_IDENTIFIER_INVALID);
    }
}
