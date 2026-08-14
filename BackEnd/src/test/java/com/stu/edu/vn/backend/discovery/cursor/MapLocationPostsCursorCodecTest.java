package com.stu.edu.vn.backend.discovery.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Kiểm tra cursor opaque, version và ràng buộc Location. */
class MapLocationPostsCursorCodecTest {
    private CursorCodec cursorCodec;

    @BeforeEach
    void setUp() {
        cursorCodec = new CursorCodec(new ObjectMapper());
    }

    @Test
    void roundTripsOpaqueVersionedCursorAndBindsLocation() {
        MapLocationPostsCursor cursor = new MapLocationPostsCursor(
                1, 15L, LocalDateTime.of(2026, 8, 14, 1, 20), 100L);

        String encoded = cursorCodec.encode(cursor);

        assertThat(encoded).doesNotContain("+", "/", "=");
        assertThat(cursorCodec.decode(encoded, MapLocationPostsCursor.class)).isEqualTo(cursor);
        assertThat(cursor.isValidFor(15L)).isTrue();
        assertThat(cursor.isValidFor(20L)).isFalse();
    }

    @Test
    void rejectsMalformedAndInvalidPayload() {
        assertThatThrownBy(() -> cursorCodec.decode("%%%", MapLocationPostsCursor.class))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR));
        assertThat(new MapLocationPostsCursor(2, 15L, LocalDateTime.now(), 1L).isValidFor(15L)).isFalse();
        assertThat(new MapLocationPostsCursor(1, 15L, null, 1L).isValidFor(15L)).isFalse();
    }
}
