package com.stu.edu.vn.backend.auth.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class EmailNormalizerTest {

    @Test
    void normalizesAndDetectsEmail() {
        NormalizedEmail result = EmailNormalizer.normalize(" Student@Example.COM ");

        assertThat(result.value()).isEqualTo("student@example.com");
    }



    @Test
    void rejectsInvalidIdentifierAndDoesNotRemoveInternalEmailSpaces() {
        assertThatThrownBy(() -> EmailNormalizer.normalize("student @example.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_IDENTIFIER_INVALID);
    }
}

