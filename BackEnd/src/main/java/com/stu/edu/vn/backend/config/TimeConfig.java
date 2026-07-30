package com.stu.edu.vn.backend.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình thời gian tập trung để các nghiệp vụ theo ngày có thể kiểm thử ổn định.
 */
@Configuration
public class TimeConfig {
    @Bean
    public Clock clock() {
        // Timestamp MySQL/API của dự án dùng UTC; không phụ thuộc múi giờ máy chạy Backend.
        return Clock.systemUTC();
    }
}
