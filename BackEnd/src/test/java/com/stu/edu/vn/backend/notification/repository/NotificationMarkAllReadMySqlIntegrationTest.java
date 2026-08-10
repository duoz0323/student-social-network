package com.stu.edu.vn.backend.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.notification.enums.NotificationType;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Xác minh native UPDATE trên MySQL không mark read Notification bị Block ẩn.
 */
@SpringBootTest(properties = "bootstrap-admin.enabled=false")
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class NotificationMarkAllReadMySqlIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final List<Long> userIds = new ArrayList<>();

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @AfterEach
    void cleanUp() {
        if (userIds.isEmpty()) return;
        for (Long userId : userIds) {
            jdbcTemplate.update("DELETE FROM notifications WHERE recipient_id = ? OR actor_id = ?", userId, userId);
            jdbcTemplate.update("DELETE FROM user_blocks WHERE blocker_id = ? OR blocked_id = ?", userId, userId);
        }
        for (Long userId : userIds) {
            userProfileRepository.deleteById(userId);
            userRepository.deleteById(userId);
        }
        userIds.clear();
    }

    @Test
    void markAllReadOnlyUpdatesVisibleNotifications() {
        User recipient = saveCompletedUser("recipient");
        User visibleActor = saveCompletedUser("visible");
        User blockedActor = saveCompletedUser("blocked");
        jdbcTemplate.update(
                "INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (?, ?)",
                recipient.getId(), blockedActor.getId());
        jdbcTemplate.update(
                "INSERT INTO notifications(recipient_id, actor_id, type) VALUES (?, ?, ?)",
                recipient.getId(), visibleActor.getId(), NotificationType.FOLLOW.name());
        jdbcTemplate.update(
                "INSERT INTO notifications(recipient_id, actor_id, type) VALUES (?, ?, ?)",
                recipient.getId(), blockedActor.getId(), NotificationType.FOLLOW.name());

        int updated = transactionTemplate.execute(status ->
                notificationRepository.markAllRead(recipient.getId(), LocalDateTime.now()));

        assertThat(updated).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT read_at IS NOT NULL FROM notifications WHERE recipient_id = ? AND actor_id = ?",
                Boolean.class, recipient.getId(), visibleActor.getId())).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT read_at IS NULL FROM notifications WHERE recipient_id = ? AND actor_id = ?",
                Boolean.class, recipient.getId(), blockedActor.getId())).isTrue();
    }

    private User saveCompletedUser(String prefix) {
        User user = new User(prefix + "-" + UUID.randomUUID() + "@example.com", "hash");
        // Tài khoản local có mật khẩu phải xác minh email trước khi lưu theo constraint Auth.
        user.setEmailVerifiedAt(LocalDateTime.now());
        user = userRepository.saveAndFlush(user);
        UserProfile profile = new UserProfile(user);
        profile.setUsername("nt_" + user.getId());
        profile.setDisplayName(prefix);
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(LocalDateTime.now());
        userProfileRepository.saveAndFlush(profile);
        userIds.add(user.getId());
        return user;
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable " + name);
        }
        return value;
    }
}
