package com.stu.edu.vn.backend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

/** Kiểm chứng row lock của Refresh Token trên MySQL test riêng, không dùng database thật. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class RefreshTokenConcurrencyMySqlIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void concurrentRefreshRequestsSerializeAndSecondRequestSeesRevokedToken() throws Exception {
        String tokenHash = "c".repeat(64);
        Long userId = inNewTransaction(() -> {
            User user = new User("refresh-race@example.com", "bcrypt-hash");
            // Fixture local-login phải có định danh đã xác minh để tuân thủ CHECK constraint production.
            user.setEmailVerifiedAt(LocalDateTime.now());
            user = userRepository.saveAndFlush(user);
            refreshTokenRepository.saveAndFlush(new RefreshToken(
                    user,
                    tokenHash,
                    LocalDateTime.now().plusHours(1)
            ));
            return user.getId();
        });
        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> first = executor.submit(() -> inNewTransaction(() -> {
                RefreshToken token = refreshTokenRepository.findByTokenHashForUpdate(tokenHash).orElseThrow();
                // Dùng timestamp đã lưu bởi MySQL để không tạo revoked_at sớm hơn created_at do lệch clock JVM/DB.
                token.revoke(token.getCreatedAt());
                firstLockAcquired.countDown();
                await(releaseFirst);
                return null;
            }));
            assertTrue(firstLockAcquired.await(5, TimeUnit.SECONDS));

            Future<Boolean> second = executor.submit(() -> inNewTransaction(() -> {
                secondStarted.countDown();
                return refreshTokenRepository.findByTokenHashForUpdate(tokenHash).orElseThrow().getRevokedAt() != null;
            }));
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            assertFalse(waitUntilDone(second, Duration.ofMillis(500)), "Request thứ hai phải chờ rotation đầu commit");

            releaseFirst.countDown();
            first.get(5, TimeUnit.SECONDS);
            assertThat(second.get(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            releaseFirst.countDown();
            inNewTransaction(() -> {
                userRepository.deleteById(userId);
                return null;
            });
        }
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

    private boolean waitUntilDone(Future<?> future, Duration duration) throws InterruptedException {
        long deadline = System.nanoTime() + duration.toNanos();
        while (System.nanoTime() < deadline) {
            if (future.isDone()) {
                return true;
            }
            Thread.sleep(10);
        }
        return future.isDone();
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Hết thời gian chờ giải phóng Refresh Token lock");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread bị ngắt khi chờ Refresh Token lock", exception);
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL dùng riêng cho integration test");
        }
        return value;
    }
}

