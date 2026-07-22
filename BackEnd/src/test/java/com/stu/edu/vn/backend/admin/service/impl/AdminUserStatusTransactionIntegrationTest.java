package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import com.stu.edu.vn.backend.admin.dto.request.AdminBlockUserRequest;
import com.stu.edu.vn.backend.admin.entity.AccountStatusHistory;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.stu.edu.vn.backend.admin.repository.AccountStatusHistoryRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.service.AdminUserService;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Test MySQL tùy chọn chứng minh transaction rollback và khóa bi quan trong điều kiện chạy thật.
 */
@SpringBootTest(properties = "bootstrap-admin.enabled=false")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
@Import(AdminUserStatusTransactionIntegrationTest.FixedClockConfiguration.class)
class AdminUserStatusTransactionIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 14, 8, 0);

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoSpyBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoSpyBean
    private AccountStatusHistoryRepository historyRepository;

    @MockitoSpyBean
    private AdminActionRepository actionRepository;

    private Fixture currentFixture;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        if (currentFixture == null) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            // Xóa đúng dữ liệu fixture theo ID, không tác động dữ liệu thật khác trong database.
            jdbcTemplate.update("DELETE FROM admin_actions WHERE admin_id = ? OR (target_type = 'USER' AND target_id = ?)",
                    currentFixture.adminId(), currentFixture.userId());
            jdbcTemplate.update("DELETE FROM account_status_histories WHERE user_id = ? OR changed_by = ?",
                    currentFixture.userId(), currentFixture.adminId());
            jdbcTemplate.update("DELETE FROM refresh_tokens WHERE user_id = ?", currentFixture.userId());
            jdbcTemplate.update("DELETE FROM users WHERE id IN (?, ?)",
                    currentFixture.userId(), currentFixture.adminId());
        });
    }

    @Test
    void blockAndUnblockPersistStateTokenAndBothAuditTables() {
        Fixture fixture = createFixture(true);
        authenticate(fixture.adminId());

        var blocked = adminUserService.blockUser(
                fixture.userId(), new AdminBlockUserRequest(AdminBlockReason.OTHER));

        assertThat(blocked.status()).isEqualTo(UserStatus.BLOCKED);
        assertThat(blocked.blockedAt()).isEqualTo(NOW);
        assertThat(blocked.blockedReason()).isEqualTo("OTHER");
        assertThat(blocked.updatedAt()).isNotNull();
        assertThat(tokenRevokedAt(fixture.activeTokenHash())).isEqualTo(NOW);
        assertThat(tokenRevokedAt(fixture.alreadyRevokedTokenHash())).isEqualTo(fixture.originalRevokedAt());
        assertThat(tokenRevokedAt(fixture.expiredTokenHash())).isNull();
        assertThat(historyRows(fixture.userId())).containsExactly("ACTIVE|BLOCKED|OTHER");
        assertThat(actionRows(fixture.userId())).containsExactly("BLOCK_USER|USER|OTHER|null|null");

        var unblocked = adminUserService.unblockUser(fixture.userId());

        assertThat(unblocked.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(unblocked.blockedAt()).isNull();
        assertThat(unblocked.blockedReason()).isNull();
        // Mở khóa không khôi phục Refresh Token vừa bị thu hồi khi khóa.
        assertThat(tokenRevokedAt(fixture.activeTokenHash())).isEqualTo(NOW);
        assertThat(historyRows(fixture.userId())).containsExactly(
                "ACTIVE|BLOCKED|OTHER", "BLOCKED|ACTIVE|ADMIN_UNBLOCK");
        assertThat(actionRows(fixture.userId())).containsExactly(
                "BLOCK_USER|USER|OTHER|null|null", "UNBLOCK_USER|USER|ADMIN_UNBLOCK|null|null");
    }

    @Test
    void revokeFailureRollsBackUserAndLeavesNoOrphanAudit() {
        Fixture fixture = createFixture(true);
        authenticate(fixture.adminId());
        doThrow(new IllegalStateException("revoke failed intentionally"))
                .when(refreshTokenRepository).revokeAllActiveByUserId(eq(fixture.userId()), any());

        assertThatThrownBy(() -> adminUserService.blockUser(
                fixture.userId(), new AdminBlockUserRequest(AdminBlockReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);

        assertRolledBack(fixture);
    }

    @Test
    void historyFailureRollsBackUserTokenAndLeavesNoOrphanAction() {
        Fixture fixture = createFixture(true);
        authenticate(fixture.adminId());
        doThrow(new IllegalStateException("history failed intentionally"))
                .when(historyRepository).save(any(AccountStatusHistory.class));

        assertThatThrownBy(() -> adminUserService.blockUser(
                fixture.userId(), new AdminBlockUserRequest(AdminBlockReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);

        assertRolledBack(fixture);
    }

    @Test
    void adminActionFailureRollsBackUserTokenAndHistory() {
        Fixture fixture = createFixture(true);
        authenticate(fixture.adminId());
        doThrow(new IllegalStateException("action failed intentionally"))
                .when(actionRepository).save(any(AdminAction.class));

        assertThatThrownBy(() -> adminUserService.blockUser(
                fixture.userId(), new AdminBlockUserRequest(AdminBlockReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);

        assertRolledBack(fixture);
    }

    @Test
    void twoConcurrentBlocksProduceOneSuccessAndOneAuditPair() throws Exception {
        Fixture fixture = createFixture(false);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<String>> results = List.of(
                    executor.submit(() -> blockConcurrently(fixture, ready, start)),
                    executor.submit(() -> blockConcurrently(fixture, ready, start))
            );
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            assertThat(List.of(
                    results.get(0).get(20, TimeUnit.SECONDS),
                    results.get(1).get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("SUCCESS", ErrorCode.ADMIN_USER_ALREADY_BLOCKED.name());
            assertThat(count("account_status_histories", fixture.userId())).isEqualTo(1);
            assertThat(count("admin_actions", fixture.userId())).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private String blockConcurrently(Fixture fixture, CountDownLatch ready, CountDownLatch start) throws Exception {
        authenticate(fixture.adminId());
        ready.countDown();
        start.await(10, TimeUnit.SECONDS);
        try {
            adminUserService.blockUser(fixture.userId(), new AdminBlockUserRequest(AdminBlockReason.SPAM));
            return "SUCCESS";
        } catch (BusinessException exception) {
            return exception.getErrorCode().name();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private Fixture createFixture(boolean includeTokens) {
        currentFixture = transactionTemplate.execute(status -> {
            String marker = UUID.randomUUID().toString().replace("-", "");
            User admin = new User("status-admin-" + marker + "@example.com", "hash");
            admin.setRole(UserRole.ADMIN);
            admin = userRepository.saveAndFlush(admin);
            User target = new User("status-user-" + marker + "@example.com", "hash");
            target = userRepository.saveAndFlush(target);

            String activeHash = tokenHash(marker, "a");
            String revokedHash = tokenHash(marker, "r");
            String expiredHash = tokenHash(marker, "e");
            LocalDateTime originalRevokedAt = NOW.minusDays(1);
            if (includeTokens) {
                insertToken(target.getId(), activeHash, NOW.minusDays(2), NOW.plusDays(2), null);
                insertToken(target.getId(), revokedHash, NOW.minusDays(2), NOW.plusDays(2), originalRevokedAt);
                insertToken(target.getId(), expiredHash, NOW.minusDays(3), NOW.minusDays(1), null);
            }
            return new Fixture(
                    admin.getId(), target.getId(), activeHash, revokedHash, expiredHash, originalRevokedAt);
        });
        return currentFixture;
    }

    private void insertToken(
            Long userId,
            String tokenHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO refresh_tokens(user_id, token_hash, expires_at, revoked_at, created_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                userId, tokenHash, expiresAt, revokedAt, createdAt);
    }

    private void authenticate(Long adminId) {
        CustomUserPrincipal principal = new CustomUserPrincipal(adminId, UserRole.ADMIN, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private void assertRolledBack(Fixture fixture) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM users WHERE id = ?", String.class, fixture.userId())).isEqualTo("ACTIVE");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT blocked_at FROM users WHERE id = ?", LocalDateTime.class, fixture.userId())).isNull();
        assertThat(tokenRevokedAt(fixture.activeTokenHash())).isNull();
        assertThat(count("account_status_histories", fixture.userId())).isZero();
        assertThat(count("admin_actions", fixture.userId())).isZero();
    }

    private LocalDateTime tokenRevokedAt(String tokenHash) {
        return jdbcTemplate.query(
                "SELECT revoked_at FROM refresh_tokens WHERE token_hash = ?",
                resultSet -> resultSet.next() ? resultSet.getObject(1, LocalDateTime.class) : null,
                tokenHash);
    }

    private List<String> historyRows(Long userId) {
        return jdbcTemplate.query(
                """
                        SELECT old_status, new_status, reason
                        FROM account_status_histories
                        WHERE user_id = ? ORDER BY id
                        """,
                (row, index) -> row.getString(1) + "|" + row.getString(2) + "|" + row.getString(3), userId);
    }

    private List<String> actionRows(Long userId) {
        return jdbcTemplate.query(
                """
                        SELECT action_type, target_type, note, old_data, new_data
                        FROM admin_actions
                        WHERE target_type = 'USER' AND target_id = ? ORDER BY id
                        """,
                (row, index) -> row.getString(1) + "|" + row.getString(2) + "|" + row.getString(3)
                        + "|" + row.getString(4) + "|" + row.getString(5), userId);
    }

    private int count(String table, Long userId) {
        String sql = "account_status_histories".equals(table)
                ? "SELECT COUNT(*) FROM account_status_histories WHERE user_id = ?"
                : "SELECT COUNT(*) FROM admin_actions WHERE target_type = 'USER' AND target_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    private String tokenHash(String marker, String suffix) {
        return (marker + suffix + "x".repeat(64)).substring(0, 64);
    }

    private record Fixture(
            Long adminId,
            Long userId,
            String activeTokenHash,
            String alreadyRevokedTokenHash,
            String expiredTokenHash,
            LocalDateTime originalRevokedAt
    ) {
    }

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedAdminStatusClock() {
            return Clock.fixed(Instant.parse("2026-07-14T01:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }
}
