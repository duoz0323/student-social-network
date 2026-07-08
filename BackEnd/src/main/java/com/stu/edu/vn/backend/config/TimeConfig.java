package com.stu.edu.vn.backend.config;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình thời gian tập trung để toàn bộ nghiệp vụ và response dùng múi giờ Việt Nam.
 */
@Configuration
public class TimeConfig {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @PostConstruct
    public void configureDefaultTimeZone() {
        // Ép JVM dùng múi giờ Việt Nam để LocalDateTime.now() trong response wrapper không bị lệch UTC.
        TimeZone.setDefault(TimeZone.getTimeZone(VIETNAM_ZONE));
    }

    @Bean
    public Clock clock() {
        // Clock dùng chung cho các rule nghiệp vụ theo thời gian, ví dụ giới hạn sửa bài trong 15 phút.
        return Clock.system(VIETNAM_ZONE);
    }
}
