package com.stu.edu.vn.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình JWT lấy từ biến môi trường, không hard-code secret trong mã nguồn.
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String accessTokenSecret;
    private String refreshTokenSecret;
    private long accessTokenExpirationMillis;
    private long refreshTokenExpirationMillis;

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    public void setRefreshTokenSecret(String refreshTokenSecret) {
        this.refreshTokenSecret = refreshTokenSecret;
    }

    public long getAccessTokenExpirationMillis() {
        return accessTokenExpirationMillis;
    }

    public void setAccessTokenExpirationMillis(long accessTokenExpirationMillis) {
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
    }

    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpirationMillis;
    }

    public void setRefreshTokenExpirationMillis(long refreshTokenExpirationMillis) {
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }
}
