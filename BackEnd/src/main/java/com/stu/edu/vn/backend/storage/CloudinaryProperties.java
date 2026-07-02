package com.stu.edu.vn.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Thuộc tính Cloudinary lấy từ biến môi trường, không chứa secret hard-code trong source.
 */
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String avatarFolder = "student-social-network/avatars";

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getAvatarFolder() {
        return avatarFolder;
    }

    public void setAvatarFolder(String avatarFolder) {
        this.avatarFolder = avatarFolder;
    }

    public boolean isConfigured() {
        return hasText(cloudName) && hasText(apiKey) && hasText(apiSecret);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
