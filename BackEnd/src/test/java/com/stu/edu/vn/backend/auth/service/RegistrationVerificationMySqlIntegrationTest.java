package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Integration test chỉ chạy với MySQL container riêng và không bao giờ trỏ database thật. */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "auth.registration.otp-hmac-secret=phase3-otp-secret-for-integration-test",
        "auth.registration.flow-token-hmac-secret=phase3-flow-secret-for-integration-test",
        "jwt.access-token-secret=phase3-jwt-secret-must-be-at-least-32-bytes-long",
        "jwt.access-token-expiration-millis=900000",
        "jwt.refresh-token-expiration-millis=2592000000"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class RegistrationVerificationMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RegistrationVerificationService verificationService;

    @Autowired
    private PendingRegistrationRepository pendingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
    void verifyEmailEndpointCreatesOneAccountAndAccessTokenCanCallOnboarding() throws Exception {
        String rawFlowToken = "phase3-email-flow-token";
        pendingRepository.saveAndFlush(pending(
                RegistrationType.EMAIL,
                "phase3-email@example.com",
                rawFlowToken
        ));

        String response = mockMvc.perform(post("/api/v1/auth/registrations/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyRegistrationRequest(
                                rawFlowToken,
                                "123456",
                                "device-1",
                                "Integration test"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileCompleted").value(false))
                .andExpect(jsonPath("$.data.nextStep").value("ONBOARDING"))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(response).path("data").path("accessToken").asText();
        User user = userRepository.findByEmail("phase3-email@example.com").orElseThrow();
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        assertThat(profileRepository.findById(user.getId())).isPresent();
        assertThat(refreshTokenRepository.findAll()).anyMatch(token -> token.getUser().getId().equals(user.getId()));

        mockMvc.perform(get("/api/v1/users/me/onboarding")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileCompleted").value(false));
    }

    @Test
    void verifyPhoneEndpointCreatesVerifiedPhoneAccount() throws Exception {
        String rawFlowToken = "phase3-phone-flow-token";
        pendingRepository.saveAndFlush(pending(RegistrationType.PHONE, "0987654321", rawFlowToken));

        mockMvc.perform(post("/api/v1/auth/registrations/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyRegistrationRequest(
                                rawFlowToken,
                                "123456",
                                null,
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("USER"));

        User user = userRepository.findByPhoneNumber("0987654321").orElseThrow();
        assertThat(user.getPhoneVerifiedAt()).isNotNull();
        assertThat(user.getEmail()).isNull();
        assertThat(profileRepository.findById(user.getId())).isPresent();
    }

    @Test
    void twoConcurrentVerificationsCreateOnlyOneUserProfileAndRefreshToken() throws Exception {
        String identifier = "phase3-race@example.com";
        String rawFlowToken = "phase3-race-flow-token";
        pendingRepository.saveAndFlush(pending(RegistrationType.EMAIL, identifier, rawFlowToken));
        VerifyRegistrationRequest request = new VerifyRegistrationRequest(rawFlowToken, "123456", null, null);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> futures = List.of(
                    executor.submit(() -> verifyConcurrently(request, ready, start)),
                    executor.submit(() -> verifyConcurrently(request, ready, start))
            );
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int successes = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(15, TimeUnit.SECONDS)) {
                    successes++;
                }
            }
            assertThat(successes).isEqualTo(1);
        } finally {
            start.countDown();
        }

        User user = userRepository.findByEmail(identifier).orElseThrow();
        assertThat(profileRepository.findById(user.getId())).isPresent();
        assertThat(refreshTokenRepository.findAll()
                .stream()
                .filter(token -> token.getUser().getId().equals(user.getId())))
                .hasSize(1);
        assertThat(pendingRepository.findAll()
                .stream()
                .filter(pending -> identifier.equals(pending.getIdentifierNormalized())))
                .singleElement()
                .extracting(PendingRegistration::getStatus)
                .isEqualTo(OtpChallengeStatus.COMPLETED);
    }

    private boolean verifyConcurrently(
            VerifyRegistrationRequest request,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Không nhận được tín hiệu bắt đầu concurrency test");
        }
        try {
            VerifyRegistrationResponse ignored = verificationService.verify(request, null);
            return true;
        } catch (BusinessException expectedLoser) {
            return false;
        }
    }

    private PendingRegistration pending(RegistrationType type, String identifier, String rawFlowToken) {
        LocalDateTime now = LocalDateTime.now();
        return PendingRegistration.start(
                type,
                identifier,
                "$2a$10$integration-test-password-hash",
                hmacService.hashFlowToken(rawFlowToken),
                hmacService.hashOtp("123456"),
                now.plusMinutes(10),
                now.plusSeconds(60),
                now.plusHours(24)
        );
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL container riêng cho integration test");
        }
        return value;
    }
}
