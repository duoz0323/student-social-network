package com.stu.edu.vn.backend.common.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class CursorCodecTest {

    private CursorCodec cursorCodec;

    @BeforeEach
    void setUp() {
        // Đăng ký Java Time module giống ObjectMapper của ứng dụng để kiểm tra đúng payload cursor thực tế.
        cursorCodec = new CursorCodec(new ObjectMapper());
    }

    @Test
    void shouldRoundTripTimeCursorAsOpaqueBase64Url() {
        TimeCursor expected = new TimeCursor(LocalDateTime.of(2026, 7, 24, 10, 30), 99L);

        String encoded = cursorCodec.encode(expected);

        assertThat(encoded).doesNotContain("+", "/", "=");
        assertThat(cursorCodec.decode(encoded, TimeCursor.class)).isEqualTo(expected);
    }

    @Test
    void shouldRejectMalformedBase64AndBlankCursor() {
        assertInvalidCursor("%%%not-base64%%%");
        assertInvalidCursor(" ");
    }

    @Test
    void shouldDecodeMissingFieldForServiceValidationWithoutLeakingTechnicalError() {
        String missingPostId = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"createdAt\":\"2026-07-24T10:30:00\"}"
                        .getBytes(StandardCharsets.UTF_8));

        TimeCursor decoded = cursorCodec.decode(missingPostId, TimeCursor.class);

        // Codec chỉ giải mã; Service chịu trách nhiệm kiểm tra đầy đủ khóa ORDER BY.
        assertThat(decoded.isValid()).isFalse();
    }

    private void assertInvalidCursor(String value) {
        assertThatThrownBy(() -> cursorCodec.decode(value, TimeCursor.class))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR));
    }
}
