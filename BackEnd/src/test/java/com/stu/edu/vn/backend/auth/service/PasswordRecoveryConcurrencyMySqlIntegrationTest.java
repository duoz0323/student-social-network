package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.*;
import com.stu.edu.vn.backend.auth.entity.PasswordRecoveryChallenge;
import com.stu.edu.vn.backend.auth.enums.PasswordRecoveryStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.repository.PasswordRecoveryChallengeRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Chạy trên MySQL test riêng để kiểm chứng PESSIMISTIC_WRITE, không trỏ database thật. */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "auth.registration.otp-hmac-secret=recovery-otp-secret-for-integration-test",
        "auth.registration.flow-token-hmac-secret=recovery-flow-secret-for-integration-test",
        "jwt.access-token-secret=recovery-jwt-secret-must-be-at-least-32-bytes",
        "jwt.access-token-expiration-millis=900000", "jwt.refresh-token-expiration-millis=2592000000"
})
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class PasswordRecoveryConcurrencyMySqlIntegrationTest {
    @Autowired PasswordRecoveryService service;
    @Autowired PasswordRecoveryChallengeRepository challenges;
    @Autowired UserRepository users;
    @Autowired AuthHmacService hmac;
    @Autowired PasswordEncoder encoder;

    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
    }

    @Test void twoConcurrentVerifyRequestsIssueExactlyOneResetAuthorizedToken() throws Exception {
        User user = user("recovery-race-verify@example.com");
        String flow = "verify-race-token";
        LocalDateTime now = LocalDateTime.now();
        challenges.saveAndFlush(PasswordRecoveryChallenge.start(user, hmac.hashFlowToken("subject-verify"),
                RegistrationType.EMAIL, hmac.hashFlowToken(flow), hmac.hashOtp("123456"),
                now.plusMinutes(10), now.minusSeconds(1), now.plusMinutes(15)));

        int successes = race(() -> service.verify(flow, new VerifyPasswordRecoveryRequest("123456")));
        assertThat(successes).isEqualTo(1);
        assertThat(challenges.findAll()).filteredOn(c -> c.getUser() != null && c.getUser().getId().equals(user.getId()))
                .anyMatch(c -> c.getStatus() == PasswordRecoveryStatus.VERIFIED);
    }

    @Test void twoConcurrentCompleteRequestsChangePasswordExactlyOnce() throws Exception {
        User user = user("recovery-race-complete@example.com");
        String reset = "complete-race-token";
        LocalDateTime now = LocalDateTime.now();
        PasswordRecoveryChallenge challenge = PasswordRecoveryChallenge.start(user, hmac.hashFlowToken("subject-complete"),
                RegistrationType.EMAIL, hmac.hashFlowToken("flow-complete"), hmac.hashOtp("123456"),
                now.plusMinutes(10), now.minusSeconds(1), now.plusMinutes(15));
        challenge.verify(hmac.hashFlowToken(reset), now.plusMinutes(5), now);
        challenges.saveAndFlush(challenge);

        int successes = race(() -> service.complete(reset,
                new CompletePasswordRecoveryRequest("ChangedStrong1!", "ChangedStrong1!")));
        assertThat(successes).isEqualTo(1);
        assertThat(encoder.matches("ChangedStrong1!", users.findById(user.getId()).orElseThrow().getPasswordHash())).isTrue();
    }

    private int race(Callable<?> operation) throws Exception {
        CountDownLatch ready = new CountDownLatch(2); CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> results = List.of(executor.submit(() -> invoke(operation, ready, start)),
                    executor.submit(() -> invoke(operation, ready, start)));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue(); start.countDown();
            int successes = 0; for (Future<Boolean> result : results) if (result.get(15, TimeUnit.SECONDS)) successes++;
            return successes;
        } finally { start.countDown(); }
    }
    private boolean invoke(Callable<?> operation, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown(); start.await(5, TimeUnit.SECONDS);
        try { operation.call(); return true; } catch (BusinessException expected) { return false; }
    }
    private User user(String email) {
        User user = new User(email, encoder.encode("CurrentStrong1!"));
        user.setEmailVerifiedAt(LocalDateTime.now().minusDays(1)); return users.saveAndFlush(user);
    }
    private static String required(String name) {
        String value = System.getenv(name); if (value == null || value.isBlank()) throw new IllegalStateException(name + " missing");
        return value;
    }
}

