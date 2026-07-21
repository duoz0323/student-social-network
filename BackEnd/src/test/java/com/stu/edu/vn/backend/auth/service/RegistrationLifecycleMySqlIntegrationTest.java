package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.delivery.RegistrationOtpSender;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Integration/concurrency test chỉ chạy với MySQL test riêng qua AUTH_TEST_DB_URL. */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "auth.registration.otp-hmac-secret=phase4-otp-secret-for-integration-test",
        "auth.registration.flow-token-hmac-secret=phase4-flow-secret-for-integration-test",
        "jwt.access-token-secret=phase4-jwt-secret-must-be-at-least-32-bytes-long",
        "jwt.access-token-expiration-millis=900000",
        "jwt.refresh-token-expiration-millis=2592000000"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class RegistrationLifecycleMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PendingRegistrationRepository pendingRepository;

    @Autowired
    private RegistrationLifecycleTransactionService lifecycleTransactionService;

    @Autowired
    private RegistrationVerificationService verificationService;

    @Autowired
    private AuthHmacService hmacService;

    @MockitoBean
    private RegistrationOtpSender otpSender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void lifecycleEndpointsUseBodyOrHeaderContractWithoutRealProvider() throws Exception {
        String rawFlowToken = "phase4-endpoint-" + UUID.randomUUID();
        String identifier = uniqueEmail("endpoint");
        pendingRepository.saveAndFlush(pending(rawFlowToken, identifier));
        when(otpSender.send(any(), any(), any())).thenReturn(OtpDeliveryResult.sent());

        mockMvc.perform(post("/api/v1/auth/registrations/resend")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ResendRegistrationRequest(rawFlowToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.registrationFlowToken").doesNotExist());

        mockMvc.perform(get("/api/v1/auth/registrations/status")
                        .header("X-Auth-Flow-Token", rawFlowToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/api/v1/auth/registrations/cancel")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CancelRegistrationRequest(rawFlowToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        mockMvc.perform(post("/api/v1/auth/registrations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                identifier,
                                "Password@123",
                                "Password@123"
                        ))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/api/v1/auth/registrations/cancel")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CancelRegistrationRequest(rawFlowToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void twoConcurrentResendsPublishOnlyOneOtpVersion() throws Exception {
        String rawFlowToken = "phase4-resend-race-" + UUID.randomUUID();
        PendingRegistration pending = pending(rawFlowToken, uniqueEmail("resend-race"));
        pendingRepository.saveAndFlush(pending);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> futures = List.of(
                    executor.submit(() -> resendConcurrently(rawFlowToken, ready, start)),
                    executor.submit(() -> resendConcurrently(rawFlowToken, ready, start))
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

        PendingRegistration stored = pendingRepository.findById(pending.getId()).orElseThrow();
        assertThat(stored.getOtpVersion()).isEqualTo(2);
        assertThat(stored.getResendCount()).isEqualTo(1);
    }

    @Test
    void resendAndVerifyOldOtpHaveOnlyOneWinningStateTransition() throws Exception {
        String rawFlowToken = "phase4-resend-verify-" + UUID.randomUUID();
        PendingRegistration pending = pending(rawFlowToken, uniqueEmail("resend-verify"));
        pendingRepository.saveAndFlush(pending);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Boolean> resend = executor.submit(() -> resendConcurrently(rawFlowToken, ready, start));
            Future<Boolean> verify = executor.submit(() -> verifyConcurrently(rawFlowToken, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(resend.get(15, TimeUnit.SECONDS), verify.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            start.countDown();
        }

        PendingRegistration stored = pendingRepository.findById(pending.getId()).orElseThrow();
        assertThat(stored.getStatus()).isIn(OtpChallengeStatus.PENDING, OtpChallengeStatus.COMPLETED);
    }

    @Test
    void cancelAndVerifyHaveOnlyOneTerminalWinner() throws Exception {
        String rawFlowToken = "phase4-cancel-verify-" + UUID.randomUUID();
        PendingRegistration pending = pending(rawFlowToken, uniqueEmail("cancel-verify"));
        pendingRepository.saveAndFlush(pending);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Boolean> cancel = executor.submit(() -> cancelConcurrently(rawFlowToken, ready, start));
            Future<Boolean> verify = executor.submit(() -> verifyConcurrently(rawFlowToken, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(cancel.get(15, TimeUnit.SECONDS), verify.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            start.countDown();
        }

        PendingRegistration stored = pendingRepository.findById(pending.getId()).orElseThrow();
        assertThat(stored.getStatus()).isIn(OtpChallengeStatus.CANCELLED, OtpChallengeStatus.COMPLETED);
        assertThat(stored.getFlowTokenHash()).isEqualTo(hmacService.hashFlowToken(rawFlowToken));
    }

    private boolean resendConcurrently(String rawFlowToken, CountDownLatch ready, CountDownLatch start) {
        await(ready, start);
        try {
            return lifecycleTransactionService.issueNewOtp(rawFlowToken).successful();
        } catch (BusinessException loser) {
            return false;
        }
    }

    private boolean cancelConcurrently(String rawFlowToken, CountDownLatch ready, CountDownLatch start) {
        await(ready, start);
        try {
            lifecycleTransactionService.cancel(rawFlowToken);
            return true;
        } catch (BusinessException loser) {
            return false;
        }
    }

    private boolean verifyConcurrently(String rawFlowToken, CountDownLatch ready, CountDownLatch start) {
        await(ready, start);
        try {
            verificationService.verify(new VerifyRegistrationRequest(rawFlowToken, "123456", null, null), null);
            return true;
        } catch (BusinessException loser) {
            return false;
        }
    }

    private void await(CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Không nhận được tín hiệu bắt đầu concurrency test");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread bị ngắt khi chờ concurrency test", exception);
        }
    }

    private PendingRegistration pending(String rawFlowToken, String identifier) {
        LocalDateTime now = LocalDateTime.now();
        return PendingRegistration.start(
                RegistrationType.EMAIL,
                identifier,
                "$2a$10$integration-test-password-hash",
                hmacService.hashFlowToken(rawFlowToken),
                hmacService.hashOtp("123456"),
                now.plusMinutes(10),
                now.minusSeconds(1),
                now.plusHours(24)
        );
    }

    private String uniqueEmail(String prefix) {
        return "phase4-" + prefix + "-" + UUID.randomUUID() + "@example.com";
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL container riêng cho integration test");
        }
        return value;
    }
}
