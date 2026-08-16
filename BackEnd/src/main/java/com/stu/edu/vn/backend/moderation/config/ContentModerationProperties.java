package com.stu.edu.vn.backend.moderation.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình kết nối FastAPI moderation chạy nội bộ. */
@ConfigurationProperties(prefix = "content-moderation")
public class ContentModerationProperties {
    private String localBaseUrl = "http://127.0.0.1:8001";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);

    public String getLocalBaseUrl() { return localBaseUrl; }
    public void setLocalBaseUrl(String localBaseUrl) { this.localBaseUrl = localBaseUrl; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
}
