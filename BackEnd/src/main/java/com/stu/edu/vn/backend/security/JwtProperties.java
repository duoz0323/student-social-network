package com.stu.edu.vn.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Cấu hình JWT lấy từ biến môi trường, không hard-code secret trong mã nguồn.
 */
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String accessTokenSecret;
    private long accessTokenExpirationMillis;
    private long refreshTokenExpirationMillis;
}
