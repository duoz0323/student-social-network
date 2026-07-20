package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình Facebook Graph API; App Secret chỉ được nạp từ môi trường triển khai. */
@ConfigurationProperties(prefix = "auth.facebook")
public class FacebookAuthProperties {
    private String appId;
    private String appSecret;
    private String graphApiVersion = "v24.0";
    private String baseUrl = "https://graph.facebook.com";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getGraphApiVersion() { return graphApiVersion; }
    public void setGraphApiVersion(String graphApiVersion) { this.graphApiVersion = graphApiVersion; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
}
