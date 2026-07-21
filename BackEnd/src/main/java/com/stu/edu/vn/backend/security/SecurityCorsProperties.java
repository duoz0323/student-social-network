package com.stu.edu.vn.backend.security;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình CORS cho frontend local và có thể mở rộng bằng biến môi trường khi triển khai. */
@Getter
@Setter
@ConfigurationProperties(prefix = "security.cors")
public class SecurityCorsProperties {
    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5174",
            "http://127.0.0.1:5174"
    ));
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "X-Auth-Flow-Token"));
    private List<String> exposedHeaders = new ArrayList<>(List.of("Authorization"));
    private boolean allowCredentials = false;
}