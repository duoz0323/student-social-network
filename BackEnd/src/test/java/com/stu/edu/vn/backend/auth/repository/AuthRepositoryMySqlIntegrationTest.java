package com.stu.edu.vn.backend.auth.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import java.time.Duration;
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

/**
 * Integration test chỉ chạy với MySQL dùng riêng cho test; tuyệt đối không trỏ vào database thật.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class AuthRepositoryMySqlIntegrationTest {

    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;

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
    void pessimisticWriteBlocksConcurrentVerificationRead() throws Exception {
        Long registrationId = inNewTransaction(() -> pendingRegistrationRepository.saveAndFlush(newPending()).getId());
        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch secondReadStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstLock = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> first = executor.submit(() -> inNewTransaction(() -> {
                pendingRegistrationRepository.findByFlowTokenHashForUpdate("mysql-lock-flow-hash").orElseThrow();
                firstLockAcquired.countDown();
                await(releaseFirstLock);
                return null;
            }));

            assertTrue(firstLockAcquired.await(5, TimeUnit.SECONDS));
            Future<?> second = executor.submit(() -> inNewTransaction(() -> {
                secondReadStarted.countDown();
                pendingRegistrationRepository.findByFlowTokenHashForUpdate("mysql-lock-flow-hash").orElseThrow();
                return null;
            }));

            assertTrue(secondReadStarted.await(5, TimeUnit.SECONDS));
            assertFalse(waitUntilDone(second, Duration.ofMillis(500)), "Request thứ hai phải chờ row lock");
            releaseFirstLock.countDown();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        } finally {
            releaseFirstLock.countDown();
            inNewTransaction(() -> {
                pendingRegistrationRepository.deleteById(registrationId);
                return null;
            });
        }
    }

    @Test
    void concurrentRequestsCannotCreateTwoActivePendingRowsForSameIdentifier() throws Exception {
        String activeKey = "EMAIL:mysql-race@example.com";
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successfulInserts = new AtomicInteger();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> first = executor.submit(() -> attemptConcurrentInsert(ready, start, successfulInserts, "1"));
            Future<?> second = executor.submit(() -> attemptConcurrentInsert(ready, start, successfulInserts, "2"));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            start.countDown();
            inNewTransaction(() -> {
                pendingRegistrationRepository.findByActiveIdentifierKey(activeKey)
                        .ifPresent(pendingRegistrationRepository::delete);
                return null;
            });
        }

        assertTrue(successfulInserts.get() == 1, "Unique active_identifier_key phải chỉ cho một insert thắng");
    }

    private void attemptConcurrentInsert(
            CountDownLatch ready,
            CountDownLatch start,
            AtomicInteger successfulInserts,
            String suffix
    ) {
        ready.countDown();
        await(start);
        try {
            inNewTransaction(() -> {
                LocalDateTime now = LocalDateTime.now();
                pendingRegistrationRepository.saveAndFlush(PendingRegistration.start(
                        RegistrationType.EMAIL,
                        "mysql-race@example.com",
                        "bcrypt-hash",
                        suffix.repeat(64),
                        (suffix.equals("1") ? "a" : "b").repeat(64),
                        now.plusMinutes(10),
                        now.plusMinutes(1),
                        now.plusHours(24)
                ));
                return null;
            });
            successfulInserts.incrementAndGet();
        } catch (org.springframework.dao.DataIntegrityViolationException expectedRaceLoss) {
            // Request thua race sẽ được RegistrationService ánh xạ sang AUTH_REGISTRATION_ALREADY_PENDING.
        }
    }

    private PendingRegistration newPending() {
        LocalDateTime now = LocalDateTime.now();
        return PendingRegistration.start(
                RegistrationType.EMAIL,
                "mysql-lock@example.com",
                "bcrypt-hash",
                "mysql-lock-flow-hash",
                "mysql-lock-otp-hash",
                now.plusMinutes(10),
                now.plusMinutes(1),
                now.plusHours(24)
        );
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
                throw new IllegalStateException("Hết thời gian chờ giải phóng row lock");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread bị ngắt khi chờ row lock", exception);
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
