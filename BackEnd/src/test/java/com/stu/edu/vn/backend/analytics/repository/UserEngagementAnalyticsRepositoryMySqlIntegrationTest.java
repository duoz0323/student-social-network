package com.stu.edu.vn.backend.analytics.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration test chạy trên MySQL test riêng đã áp dụng baseline và migration V005.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserEngagementAnalyticsRepository.class)
@EnabledIfEnvironmentVariable(named = "ANALYTICS_TEST_DB_URL", matches = "jdbc:mysql:.*")
class UserEngagementAnalyticsRepositoryMySqlIntegrationTest {

    @Autowired
    private UserEngagementAnalyticsRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("ANALYTICS_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("ANALYTICS_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("ANALYTICS_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void classifiesSixExclusiveGroupsAndSeparatesReturningEligiblePopulation() {
        Long newUser = insertEligibleUser("new");
        Long regular = insertEligibleUser("regular");
        Long returningEligible = insertEligibleUser("returning-eligible");
        Long returningNotEligible = insertEligibleUser("returning-not-eligible");
        Long recentInactive = insertEligibleUser("recent-inactive");
        Long eligibleInactive = insertEligibleUser("eligible-inactive");
        insertEligibleUser("never");

        insertActivity(newUser, "2026-06-05");
        insertActivity(newUser, "2026-06-10");
        insertActivity(regular, "2026-05-21");
        insertActivity(regular, "2026-06-05"); // Đúng 15 ngày vẫn là REGULAR.
        insertActivity(returningEligible, "2026-05-01");
        insertActivity(returningEligible, "2026-06-05");
        insertActivity(returningNotEligible, "2026-05-25");
        insertActivity(returningNotEligible, "2026-06-20");
        insertActivity(recentInactive, "2026-05-25");
        insertActivity(eligibleInactive, "2026-05-01");

        MonthlyUserEngagementCounts counts = repository.summarizeMonth(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0),
                15
        );

        assertThat(counts).isEqualTo(new MonthlyUserEngagementCounts(7, 1, 1, 2, 1, 1, 1, 1));
        assertThat(counts.eligibleSystemUserCount()).isEqualTo(
                counts.newActiveUserCount() + counts.regularActiveUserCount() + counts.returningUserCount()
                        + counts.recentlyInactiveUserCount() + counts.eligibleInactiveNotReturnedUserCount()
                        + counts.neverActiveUserCount());
    }

    private Long insertEligibleUser(String label) {
        String email = "analytics-" + label + "-" + UUID.randomUUID() + "@example.com";
        jdbcTemplate.update("""
                INSERT INTO users (email, email_verified_at, password_hash, role, status, created_at, updated_at)
                VALUES (?, '2026-01-01 00:00:00', 'test-hash', 'USER', 'ACTIVE',
                        '2026-01-01 00:00:00', '2026-01-01 00:00:00')
                """, email);
        Long id = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
        jdbcTemplate.update("""
                INSERT INTO user_profiles (
                    user_id, username, display_name, date_of_birth, profile_completed_at, created_at, updated_at
                ) VALUES (?, ?, ?, '2000-01-01', '2026-01-01 00:00:00',
                          '2026-01-01 00:00:00', '2026-01-01 00:00:00')
                """, id, "analytics_" + id, label);
        return id;
    }

    private void insertActivity(Long userId, String date) {
        jdbcTemplate.update("""
                INSERT INTO user_daily_activities (
                    user_id, activity_date, first_active_at, last_active_at, activity_count
                ) VALUES (?, ?, CONCAT(?, ' 08:00:00'), CONCAT(?, ' 08:00:00'), 1)
                """, userId, LocalDate.parse(date), date, date);
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL test riêng");
        }
        return value;
    }
}
