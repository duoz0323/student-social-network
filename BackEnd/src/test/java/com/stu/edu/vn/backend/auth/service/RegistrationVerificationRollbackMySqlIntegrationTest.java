package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Chứng minh lỗi Refresh Token làm rollback cả user, profile và completion trên MySQL. */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "auth.registration.otp-hmac-secret=phase3-rollback-otp-secret",
        "auth.registration.flow-token-hmac-secret=phase3-rollback-flow-secret",
        "jwt.access-token-secret=phase3-rollback-jwt-secret-at-least-32-bytes",
        "jwt.access-token-expiration-millis=900000",
        "jwt.refresh-token-expiration-millis=-86400000"
})
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class RegistrationVerificationRollbackMySqlIntegrationTest {

    @Autowired
    private RegistrationVerificationService verificationService;

    @Autowired
    private PendingRegistrationRepository pendingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private AuthHmacService hmacService;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void refreshTokenConstraintFailureRollsBackUserProfileAndPendingCompletion() {
        String identifier = "phase3-rollback@example.com";
        String rawFlowToken = "phase3-rollback-flow-token";
        LocalDateTime now = LocalDateTime.now();
        long profileCountBefore = profileRepository.count();
        pendingRepository.saveAndFlush(PendingRegistration.start(
                RegistrationType.EMAIL,
                identifier,
                "$2a$10$rollback-password-hash",
                hmacService.hashFlowToken(rawFlowToken),
                hmacService.hashOtp("123456"),
                now.plusMinutes(10),
                now.plusSeconds(60),
                now.plusHours(24)
        ));

        assertThatThrownBy(() -> verificationService.verify(
                new VerifyRegistrationRequest(rawFlowToken, "123456", null, null),
                null
        )).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_ACCOUNT_CREATION_FAILED);

        assertThat(userRepository.findByEmail(identifier)).isEmpty();
        // So sánh số lượng tránh truy cập quan hệ LAZY ngoài persistence context.
        assertThat(profileRepository.count()).isEqualTo(profileCountBefore);
        PendingRegistration pending = pendingRepository
                .findByActiveIdentifierKey("EMAIL:" + identifier)
                .orElseThrow();
        assertThat(pending.getStatus()).isEqualTo(OtpChallengeStatus.PENDING);
        assertThat(pending.getCompletedUser()).isNull();
        assertThat(pending.getFlowTokenHash()).isNotNull();
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL container riêng cho integration test");
        }
        return value;
    }
}

