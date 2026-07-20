package com.stu.edu.vn.backend.auth.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Tạo verifier dùng chung để tái sử dụng cache public key của Google giữa các request. */
@Configuration
public class GoogleVerifierConfig {

    @Bean
    GoogleIdTokenVerifier googleSdkIdTokenVerifier(GoogleAuthProperties properties) {
        String audience = properties.getClientId() == null || properties.getClientId().isBlank()
                ? "unconfigured-google-client-id"
                : properties.getClientId();
        try {
            return new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(List.of(audience))
                    .setAcceptableTimeSkewSeconds(properties.getClockSkew().toSeconds())
                    .build();
        } catch (GeneralSecurityException | java.io.IOException exception) {
            throw new IllegalStateException("Không thể khởi tạo Google ID Token verifier", exception);
        }
    }
}
