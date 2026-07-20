package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình scheduler cleanup; mặc định tắt để không tác động dữ liệu development khi khởi động. */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.cleanup")
public class AuthCleanupProperties {
    private boolean enabled;
    private Duration fixedDelay = Duration.ofHours(1);
    private Duration retention = Duration.ofDays(7);
    private int batchSize = 100;
}
