package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình thời hạn ngắn của reauthentication token. */
@ConfigurationProperties(prefix = "auth.reauthentication")
public class ReauthenticationProperties {

    private Duration expiration = Duration.ofMinutes(5);

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }
}
