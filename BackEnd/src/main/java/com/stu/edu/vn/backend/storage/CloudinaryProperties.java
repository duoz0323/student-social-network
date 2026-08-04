package com.stu.edu.vn.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * Thuộc tính Cloudinary lấy từ biến môi trường, không chứa secret hard-code trong source.
 */
@ConfigurationProperties(prefix = "cloudinary")
@Getter
@Setter
public class CloudinaryProperties {

    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String avatarFolder = "student-social-network/avatars";
    private String postFolder = "student-social-network/posts";
    private String messageFolder = "student-social-network/messages";
    private Duration messageAccessTtl = Duration.ofMinutes(5);

    public boolean isConfigured() {
        return hasText(cloudName) && hasText(apiKey) && hasText(apiSecret);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
