package com.stu.edu.vn.backend.discovery.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class NearbyCursorCodecTest {
    private CursorCodec cursorCodec;

    @BeforeEach
    void setUp() {
        cursorCodec = new CursorCodec(new ObjectMapper());
    }

    @Test
    void roundTripsVersionedNearbyCursorAsBase64Url() {
        NearbyCursor cursor = new NearbyCursor(
                NearbyCursor.CURRENT_VERSION,
                850L,
                LocalDateTime.of(2026, 8, 13, 10, 0),
                99L,
                "a".repeat(64));

        String encoded = cursorCodec.encode(cursor);

        assertThat(encoded).doesNotContain("+", "/", "=");
        assertThat(cursorCodec.decode(encoded, NearbyCursor.class)).isEqualTo(cursor);
        assertThat(cursor.isValid()).isTrue();
    }

    @Test
    void rejectsMalformedBase64AndLeavesWrongPayloadForServiceValidation() {
        assertThatThrownBy(() -> cursorCodec.decode("%%%", NearbyCursor.class))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR));

        String wrongPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"version\":1,\"distanceMeters\":10}".getBytes(StandardCharsets.UTF_8));
        NearbyCursor decoded = cursorCodec.decode(wrongPayload, NearbyCursor.class);
        assertThat(decoded.isValid()).isFalse();
    }
}
