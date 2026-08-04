package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Kiểm chứng composite PK, câu lệnh insert atomic và trigger counter trên MySQL thật dành riêng cho test. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "REPOST_TEST_DB_URL", matches = "jdbc:mysql:.*")
class PostRepostRepositoryMySqlIntegrationTest {
    @Autowired private PostRepostRepository postRepostRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("REPOST_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("REPOST_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("REPOST_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void concurrentInsertCreatesOneRelationAndExactCounterThenDeleteNeverGoesNegative() throws Exception {
        User author = inTransaction(() -> userRepository.saveAndFlush(newUser("author")));
        User reposter = inTransaction(() -> userRepository.saveAndFlush(newUser("reposter")));
        Post post = inTransaction(() -> postRepository.saveAndFlush(new Post(author, "Bài kiểm tra Repost concurrent")));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Integer> first = executor.submit(() -> insertTogether(reposter.getId(), post.getId(), ready, start));
            Future<Integer> second = executor.submit(() -> insertTogether(reposter.getId(), post.getId(), ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            assertThat(first.get(5, TimeUnit.SECONDS) + second.get(5, TimeUnit.SECONDS)).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM post_reposts WHERE user_id=? AND post_id=?",
                    Integer.class, reposter.getId(), post.getId())).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT repost_count FROM posts WHERE id=?", Integer.class, post.getId())).isEqualTo(1);

            assertThat(inTransaction(() -> postRepostRepository
                    .deleteByUserIdAndPostId(reposter.getId(), post.getId()))).isEqualTo(1);
            assertThat(inTransaction(() -> postRepostRepository
                    .deleteByUserIdAndPostId(reposter.getId(), post.getId()))).isZero();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT repost_count FROM posts WHERE id=?", Integer.class, post.getId())).isZero();
        } finally {
            start.countDown();
            jdbcTemplate.update("DELETE FROM posts WHERE id=?", post.getId());
            jdbcTemplate.update("DELETE FROM users WHERE id IN (?,?)", author.getId(), reposter.getId());
        }
    }

    private int insertTogether(Long userId, Long postId, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        await(start);
        return inTransaction(() -> postRepostRepository.insertIfAbsent(userId, postId));
    }

    private <T> T inTransaction(java.util.concurrent.Callable<T> action) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> {
            try {
                return action.call();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Hết thời gian chờ đồng bộ test Repost");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private User newUser(String prefix) {
        return new User(prefix + "-repost-" + UUID.randomUUID() + "@example.com", "bcrypt-test-hash");
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL riêng đã áp dụng migration Repost");
        }
        return value;
    }
}
