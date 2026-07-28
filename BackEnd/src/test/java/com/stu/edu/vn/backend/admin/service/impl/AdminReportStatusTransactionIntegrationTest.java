package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.stu.edu.vn.backend.admin.dto.request.AdminRejectReportRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminResolveReportRequest;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.service.AdminReportService;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.repository.ReportRepository;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

/** Chứng minh transaction, rollback và pessimistic lock khi ADMIN xử lý Report trên MySQL thật. */
@SpringBootTest(properties = "bootstrap-admin.enabled=false")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
@Import(AdminReportStatusTransactionIntegrationTest.FixedClockConfiguration.class)
class AdminReportStatusTransactionIntegrationTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 15, 8, 0);

    @Autowired private AdminReportService adminReportService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TransactionTemplate transactionTemplate;
    @MockitoSpyBean private AdminActionRepository actionRepository;

    private Fixture currentFixture;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        if (currentFixture == null) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            // Dọn đúng fixture theo thứ tự khóa ngoại, không tác động dữ liệu khác trong database.
            jdbcTemplate.update("""
                    DELETE FROM admin_actions
                    WHERE (target_type = 'REPORT' AND target_id IN (?, ?))
                       OR (target_type = 'POST' AND target_id = ?)
                    """, currentFixture.reportId(), currentFixture.otherReportId(), currentFixture.postId());
            jdbcTemplate.update("DELETE FROM reports WHERE id IN (?, ?)",
                    currentFixture.reportId(), currentFixture.otherReportId());
            jdbcTemplate.update("DELETE FROM posts WHERE id = ?", currentFixture.postId());
            jdbcTemplate.update("DELETE FROM users WHERE id IN (?, ?, ?, ?, ?)",
                    currentFixture.adminOneId(), currentFixture.adminTwoId(), currentFixture.authorId(),
                    currentFixture.reporterOneId(), currentFixture.reporterTwoId());
        });
    }

    @Test
    void rejectPersistsResolutionAuditAndLeavesPostUnchanged() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        authenticate(fixture.adminOneId());

        var response = adminReportService.rejectReport(
                fixture.reportId(), new AdminRejectReportRequest("  Không phát hiện vi phạm  "));

        assertThat(response.status().name()).isEqualTo("REJECTED");
        assertThat(response.resolvedAt()).isEqualTo(NOW);
        assertThat(reportState(fixture.reportId())).isEqualTo(
                "REJECTED|" + fixture.adminOneId() + "|" + timestamp(NOW) + "|Không phát hiện vi phạm");
        assertThat(postState(fixture.postId())).isEqualTo("PUBLISHED|null|null|null");
        assertThat(reportActions(fixture.reportId()))
                .containsExactly("REJECT_REPORT|REPORT|Không phát hiện vi phạm|null|null");
        assertThat(postActions(fixture.postId())).isEmpty();
        assertThat(reportStatus(fixture.otherReportId())).isEqualTo("PENDING");
    }

    @Test
    void resolveWithoutHidePersistsOnlyResolveAction() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        authenticate(fixture.adminOneId());

        adminReportService.resolveReport(fixture.reportId(),
                new AdminResolveReportRequest("Hợp lệ", false, null));

        assertThat(reportStatus(fixture.reportId())).isEqualTo("RESOLVED");
        assertThat(postState(fixture.postId())).isEqualTo("PUBLISHED|null|null|null");
        assertThat(reportActions(fixture.reportId())).containsExactly("RESOLVE_REPORT|REPORT|Hợp lệ|null|null");
        assertThat(postActions(fixture.postId())).isEmpty();
    }

    @Test
    void resolvingTwoReportsForSamePostCreatesOnlyOneHideAction() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        authenticate(fixture.adminOneId());

        adminReportService.resolveReport(fixture.reportId(),
                new AdminResolveReportRequest("Report đầu hợp lệ", true, AdminPostHideReason.SPAM));
        adminReportService.resolveReport(fixture.otherReportId(),
                new AdminResolveReportRequest("Report sau hợp lệ", true, AdminPostHideReason.HARASSMENT));

        assertThat(postState(fixture.postId())).isEqualTo(
                "HIDDEN|" + fixture.adminOneId() + "|" + timestamp(NOW) + "|SPAM");
        assertThat(reportStatus(fixture.reportId())).isEqualTo("RESOLVED");
        assertThat(reportStatus(fixture.otherReportId())).isEqualTo("RESOLVED");
        assertThat(reportActions(fixture.reportId())).containsExactly(
                "RESOLVE_REPORT|REPORT|Report đầu hợp lệ|null|null");
        assertThat(reportActions(fixture.otherReportId())).containsExactly(
                "RESOLVE_REPORT|REPORT|Report sau hợp lệ|null|null");
        assertThat(postActions(fixture.postId())).containsExactly("HIDE_POST|POST|SPAM|null|null");
    }

    @Test
    void hiddenAndDeletedPostsRemainUnchangedWithoutSyntheticHideAction() {
        for (PostStatus postStatus : List.of(PostStatus.HIDDEN, PostStatus.DELETED)) {
            Fixture fixture = createFixture(postStatus);
            authenticate(fixture.adminOneId());
            String before = postState(fixture.postId());

            adminReportService.resolveReport(fixture.reportId(),
                    new AdminResolveReportRequest("Hợp lệ", true, AdminPostHideReason.SPAM));

            assertThat(reportStatus(fixture.reportId())).isEqualTo("RESOLVED");
            assertThat(postState(fixture.postId())).isEqualTo(before);
            assertThat(postActions(fixture.postId())).isEmpty();
            cleanUp();
            currentFixture = null;
        }
    }

    @Test
    void rejectActionFailureRollsBackReportAndLeavesNoOrphanAction() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        authenticate(fixture.adminOneId());
        failOnAction(AdminActionType.REJECT_REPORT);

        assertThatThrownBy(() -> adminReportService.rejectReport(
                fixture.reportId(), new AdminRejectReportRequest("Không hợp lệ")))
                .isInstanceOf(IllegalStateException.class);

        assertRolledBack(fixture);
    }

    @Test
    void resolveActionFailureRollsBackReport() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        authenticate(fixture.adminOneId());
        failOnAction(AdminActionType.RESOLVE_REPORT);

        assertThatThrownBy(() -> adminReportService.resolveReport(fixture.reportId(),
                new AdminResolveReportRequest("Hợp lệ", false, null)))
                .isInstanceOf(IllegalStateException.class);

        assertRolledBack(fixture);
    }

    @Test
    void hideActionFailureRollsBackReportPostAndResolveAction() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        authenticate(fixture.adminOneId());
        failOnAction(AdminActionType.HIDE_POST);

        assertThatThrownBy(() -> adminReportService.resolveReport(fixture.reportId(),
                new AdminResolveReportRequest("Hợp lệ", true, AdminPostHideReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);

        assertRolledBack(fixture);
    }

    @Test
    void twoConcurrentResolvesProduceOneSuccessAndOneResolveAction() throws Exception {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        assertConcurrentResult(fixture, false);

        assertThat(reportStatus(fixture.reportId())).isEqualTo("RESOLVED");
        assertThat(reportActions(fixture.reportId())).hasSize(1)
                .allMatch(row -> row.startsWith("RESOLVE_REPORT|REPORT|"));
    }

    @Test
    void concurrentResolveAndRejectProduceOneConsistentFinalState() throws Exception {
        Fixture fixture = createFixture(PostStatus.PUBLISHED);
        assertConcurrentResult(fixture, true);

        assertThat(reportStatus(fixture.reportId())).isIn("RESOLVED", "REJECTED");
        assertThat(reportActions(fixture.reportId())).hasSize(1);
    }

    private void assertConcurrentResult(Fixture fixture, boolean mixedOperations) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(() -> mutateConcurrently(
                    fixture.adminOneId(), fixture.reportId(), false, ready, start));
            Future<String> second = executor.submit(() -> mutateConcurrently(
                    fixture.adminTwoId(), fixture.reportId(), mixedOperations, ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("SUCCESS", ErrorCode.ADMIN_REPORT_ALREADY_PROCESSED.name());
        } finally {
            executor.shutdownNow();
        }
    }

    private String mutateConcurrently(Long adminId, Long reportId, boolean reject,
            CountDownLatch ready, CountDownLatch start) throws Exception {
        authenticate(adminId);
        ready.countDown();
        start.await(10, TimeUnit.SECONDS);
        try {
            if (reject) {
                adminReportService.rejectReport(reportId, new AdminRejectReportRequest("Reject concurrently"));
            } else {
                adminReportService.resolveReport(reportId,
                        new AdminResolveReportRequest("Resolve concurrently", false, null));
            }
            return "SUCCESS";
        } catch (BusinessException exception) {
            return exception.getErrorCode().name();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void failOnAction(AdminActionType actionType) {
        doAnswer(invocation -> {
            AdminAction action = invocation.getArgument(0);
            if (action.getActionType() == actionType) {
                throw new IllegalStateException(actionType + " failed intentionally");
            }
            // Ghi action đứng trước bằng JdbcTemplate trong chính transaction để kiểm chứng nó cũng rollback.
            jdbcTemplate.update("""
                    INSERT INTO admin_actions(admin_id, action_type, target_type, target_id, note)
                    VALUES (?, ?, ?, ?, ?)
                    """, action.getAdmin().getId(), action.getActionType().name(),
                    action.getTargetType().name(), action.getTargetId(), action.getNote());
            return action;
        }).when(actionRepository).save(any(AdminAction.class));
    }

    private Fixture createFixture(PostStatus postStatus) {
        currentFixture = transactionTemplate.execute(status -> {
            String marker = UUID.randomUUID().toString().replace("-", "");
            User adminOne = saveUser("report-admin1-" + marker + "@example.com", UserRole.ADMIN);
            User adminTwo = saveUser("report-admin2-" + marker + "@example.com", UserRole.ADMIN);
            User author = saveUser("report-author-" + marker + "@example.com", UserRole.USER);
            User reporterOne = saveUser("report-user1-" + marker + "@example.com", UserRole.USER);
            User reporterTwo = saveUser("report-user2-" + marker + "@example.com", UserRole.USER);
            saveProfile(adminOne, "Admin One " + marker);
            saveProfile(adminTwo, "Admin Two " + marker);
            saveProfile(author, "Author " + marker);
            saveProfile(reporterOne, "Reporter One " + marker);
            saveProfile(reporterTwo, "Reporter Two " + marker);

            Post post = new Post(author, "Reported " + marker);
            post.setStatus(postStatus);
            if (postStatus == PostStatus.HIDDEN) {
                post.setHiddenBy(adminOne);
                post.setHiddenAt(NOW.minusHours(1));
                post.setHiddenReason("HARASSMENT");
            } else if (postStatus == PostStatus.DELETED) {
                post.setDeletedAt(NOW.minusHours(1));
            }
            post = postRepository.saveAndFlush(post);
            Report report = reportRepository.saveAndFlush(new Report(
                    reporterOne, post, ReportReason.SPAM, null, post.getContent(), "[]"));
            Report otherReport = reportRepository.saveAndFlush(new Report(
                    reporterTwo, post, ReportReason.HARASSMENT, null, post.getContent(), "[]"));
            return new Fixture(adminOne.getId(), adminTwo.getId(), author.getId(), reporterOne.getId(),
                    reporterTwo.getId(), post.getId(), report.getId(), otherReport.getId());
        });
        return currentFixture;
    }

    private User saveUser(String email, UserRole role) {
        User user = new User(email, "hash");
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private void saveProfile(User user, String displayName) {
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName(displayName);
        // Hồ sơ hoàn tất phải có ngày sinh để thỏa check constraint của schema MySQL thật.
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(NOW.minusDays(1));
        userProfileRepository.saveAndFlush(profile);
    }

    private void authenticate(Long adminId) {
        CustomUserPrincipal principal = new CustomUserPrincipal(adminId, UserRole.ADMIN, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private void assertRolledBack(Fixture fixture) {
        assertThat(reportStatus(fixture.reportId())).isEqualTo("PENDING");
        assertThat(postState(fixture.postId())).isEqualTo("PUBLISHED|null|null|null");
        assertThat(reportActions(fixture.reportId())).isEmpty();
        assertThat(postActions(fixture.postId())).isEmpty();
    }

    private String reportState(Long reportId) {
        return jdbcTemplate.queryForObject("""
                SELECT CONCAT(status, '|', COALESCE(CAST(resolved_by AS CHAR), 'null'), '|',
                              COALESCE(DATE_FORMAT(resolved_at, '%Y-%m-%dT%H:%i:%s'), 'null'), '|',
                              COALESCE(resolution_note, 'null'))
                FROM reports WHERE id = ?
                """, String.class, reportId);
    }

    private String reportStatus(Long reportId) {
        return jdbcTemplate.queryForObject("SELECT status FROM reports WHERE id = ?", String.class, reportId);
    }

    private String postState(Long postId) {
        return jdbcTemplate.queryForObject("""
                SELECT CONCAT(status, '|', COALESCE(CAST(hidden_by AS CHAR), 'null'), '|',
                              COALESCE(DATE_FORMAT(hidden_at, '%Y-%m-%dT%H:%i:%s'), 'null'), '|',
                              COALESCE(hidden_reason, 'null'))
                FROM posts WHERE id = ?
                """, String.class, postId);
    }

    private List<String> reportActions(Long reportId) {
        return actionRows("REPORT", reportId);
    }

    private List<String> postActions(Long postId) {
        return actionRows("POST", postId);
    }

    private List<String> actionRows(String targetType, Long targetId) {
        return jdbcTemplate.query("""
                SELECT action_type, target_type, note, old_data, new_data
                FROM admin_actions WHERE target_type = ? AND target_id = ? ORDER BY id
                """, (row, index) -> row.getString(1) + "|" + row.getString(2) + "|" + row.getString(3)
                        + "|" + row.getString(4) + "|" + row.getString(5), targetType, targetId);
    }

    private String timestamp(LocalDateTime value) {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private record Fixture(
            Long adminOneId,
            Long adminTwoId,
            Long authorId,
            Long reporterOneId,
            Long reporterTwoId,
            Long postId,
            Long reportId,
            Long otherReportId
    ) {
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        Clock fixedAdminReportClock() {
            return Clock.fixed(Instant.parse("2026-07-15T01:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }
}
