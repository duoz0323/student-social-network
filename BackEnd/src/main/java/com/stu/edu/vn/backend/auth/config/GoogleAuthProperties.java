package com.stu.edu.vn.backend.auth.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình xác minh Google và social challenge, mọi secret lấy từ biến môi trường. */
@ConfigurationProperties(prefix = "auth.google")
public class GoogleAuthProperties {

    private String clientId;
    private List<String> issuers = List.of("accounts.google.com", "https://accounts.google.com");
    private Duration clockSkew = Duration.ofSeconds(30);
    private Duration conflictExpiration = Duration.ofMinutes(5);
    private int conflictTokenRandomBytes = 32;
    private String identityFingerprintSecret;

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public List<String> getIssuers() { return issuers; }
    public void setIssuers(List<String> issuers) { this.issuers = issuers; }
    public Duration getClockSkew() { return clockSkew; }
    public void setClockSkew(Duration clockSkew) { this.clockSkew = clockSkew; }
    public Duration getConflictExpiration() { return conflictExpiration; }
    public void setConflictExpiration(Duration conflictExpiration) { this.conflictExpiration = conflictExpiration; }
    public int getConflictTokenRandomBytes() { return conflictTokenRandomBytes; }
    public void setConflictTokenRandomBytes(int conflictTokenRandomBytes) { this.conflictTokenRandomBytes = conflictTokenRandomBytes; }
    public String getIdentityFingerprintSecret() { return identityFingerprintSecret; }
    public void setIdentityFingerprintSecret(String identityFingerprintSecret) { this.identityFingerprintSecret = identityFingerprintSecret; }
}
