package com.stu.edu.vn.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TimeConfigTest {

    @Test
    void applicationClockAlwaysUsesUtc() {
        // Clock UTC giữ phép so sánh với DATETIME do MySQL/API đang quy ước theo UTC.
        assertThat(new TimeConfig().clock().getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
