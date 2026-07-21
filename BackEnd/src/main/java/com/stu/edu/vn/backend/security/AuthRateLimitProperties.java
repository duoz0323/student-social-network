package com.stu.edu.vn.backend.security;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Chính sách limiter có thể cấu hình; mặc định tắt cho đến khi môi trường chốt quota vận hành. */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.rate-limit")
public class AuthRateLimitProperties {
    private boolean enabled;
    private Duration window = Duration.ofMinutes(1);
    private int maxRequests = 30;
}
