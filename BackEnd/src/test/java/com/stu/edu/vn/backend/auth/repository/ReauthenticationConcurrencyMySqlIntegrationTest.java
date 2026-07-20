package com.stu.edu.vn.backend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Kiểm chứng MySQL row lock bảo đảm một reauthentication token chỉ được consume một lần. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class ReauthenticationConcurrencyMySqlIntegrationTest {

    @Autowired ReauthenticationChallengeRepository challengeRepository;
    @Autowired UserRepository userRepository;
    @Autowired PlatformTransactionManager transactionManager;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void concurrentConsumeAllowsOnlyOneTransactionToSucceed() throws Exception {
        String tokenHash = "c".repeat(64);
        Long[] ids = inNewTransaction(() -> {
            User user = new User("reauth-concurrency@example.com", null, "bcrypt-hash");
            user.setEmailVerifiedAt(LocalDateTime.now());
            User savedUser = userRepository.saveAndFlush(user);
            ReauthenticationChallenge challenge = challengeRepository.saveAndFlush(
                    ReauthenticationChallenge.start(
                            savedUser, tokenHash, ReauthenticationProofMethod.LOCAL_PASSWORD,
                            ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.GOOGLE,
                            LocalDateTime.now().plusMinutes(5)));
            return new Long[]{savedUser.getId(), challenge.getId()};
        });
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger consumed = new AtomicInteger();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> first = executor.submit(() -> consume(tokenHash, ready, start, consumed));
            Future<?> second = executor.submit(() -> consume(tokenHash, ready, start, consumed));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            start.countDown();
        }

        assertThat(consumed.get()).isEqualTo(1);
        inNewTransaction(() -> {
            ReauthenticationChallenge challenge = challengeRepository.findById(ids[1]).orElseThrow();
            assertThat(challenge.getStatus()).isEqualTo(ReauthenticationChallengeStatus.CONSUMED);
            assertThat(challenge.getTokenHash()).isNull();
            challengeRepository.delete(challenge);
            userRepository.deleteById(ids[0]);
            return null;
        });
    }

    private void consume(String tokenHash, CountDownLatch ready, CountDownLatch start, AtomicInteger consumed) {
        ready.countDown();
        await(start);
        inNewTransaction(() -> {
            challengeRepository.findByTokenHashForUpdate(tokenHash).ifPresent(challenge -> {
                if (challenge.getStatus() == ReauthenticationChallengeStatus.ACTIVE
                        && challenge.getExpiresAt().isAfter(LocalDateTime.now())) {
                    challenge.consume(LocalDateTime.now());
                    challengeRepository.saveAndFlush(challenge);
                    consumed.incrementAndGet();
                }
            });
            return null;
        });
    }

    private <T> T inNewTransaction(java.util.concurrent.Callable<T> callback) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transaction.execute(status -> {
            try {
                return callback.call();
            } catch (RuntimeException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Hết thời gian chờ bắt đầu concurrency test");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL test riêng");
        }
        return value;
    }
}
