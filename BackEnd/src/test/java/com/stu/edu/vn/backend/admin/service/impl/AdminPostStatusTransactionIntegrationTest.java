package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.stu.edu.vn.backend.admin.dto.request.AdminHidePostRequest;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.service.AdminPostService;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.service.PostService;
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

/** Chứng minh transaction, rollback và pessimistic lock của thao tác ẩn/khôi phục trên MySQL thật. */
@SpringBootTest(properties = "bootstrap-admin.enabled=false")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
@Import(AdminPostStatusTransactionIntegrationTest.FixedClockConfiguration.class)
class AdminPostStatusTransactionIntegrationTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 15, 8, 0);

    @Autowired private AdminPostService adminPostService;
    @Autowired private PostService postService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private PostRepository postRepository;
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
            // Dọn đúng fixture theo ID và theo thứ tự khóa ngoại, không tác động dữ liệu khác.
            jdbcTemplate.update("DELETE FROM admin_actions WHERE target_type = 'POST' AND target_id = ?",
                    currentFixture.postId());
            jdbcTemplate.update("DELETE FROM posts WHERE id = ?", currentFixture.postId());
            jdbcTemplate.update("DELETE FROM users WHERE id IN (?, ?, ?)", currentFixture.adminId(),
                    currentFixture.authorId(), currentFixture.viewerId());
        });
    }

    @Test
    void hideAndRestorePersistPostAuditAndUserVisibility() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED, UserStatus.ACTIVE);
        authenticate(fixture.viewerId(), UserRole.USER, UserStatus.ACTIVE);
        assertThat(postService.getPostDetail(fixture.postId()).id()).isEqualTo(fixture.postId());

        authenticate(fixture.adminId(), UserRole.ADMIN, UserStatus.ACTIVE);
        var hidden = adminPostService.hidePost(
                fixture.postId(), new AdminHidePostRequest(AdminPostHideReason.OTHER));
        assertThat(hidden.status()).isEqualTo(PostStatus.HIDDEN);
        assertThat(hidden.hiddenAt()).isEqualTo(NOW);
        assertThat(hidden.hiddenReason()).isEqualTo("OTHER");
        assertThat(hidden.hiddenBy().adminId()).isEqualTo(fixture.adminId());
        assertThat(postState(fixture.postId())).isEqualTo(
                "HIDDEN|" + fixture.adminId() + "|" + timestamp(NOW) + "|OTHER");
        assertThat(actionRows(fixture.postId())).containsExactly("HIDE_POST|POST|OTHER|null|null");

        authenticate(fixture.viewerId(), UserRole.USER, UserStatus.ACTIVE);
        assertBusinessError(() -> postService.getPostDetail(fixture.postId()), ErrorCode.POST_NOT_FOUND);
        authenticate(fixture.adminId(), UserRole.ADMIN, UserStatus.ACTIVE);
        assertThat(adminPostService.getPostDetail(fixture.postId()).status()).isEqualTo(PostStatus.HIDDEN);

        var restored = adminPostService.restorePost(fixture.postId());
        assertThat(restored.status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(restored.hiddenBy()).isNull();
        assertThat(postState(fixture.postId())).isEqualTo("PUBLISHED|null|null|null");
        assertThat(actionRows(fixture.postId())).containsExactly(
                "HIDE_POST|POST|OTHER|null|null", "RESTORE_POST|POST|ADMIN_RESTORE|null|null");

        authenticate(fixture.viewerId(), UserRole.USER, UserStatus.ACTIVE);
        assertThat(postService.getPostDetail(fixture.postId()).id()).isEqualTo(fixture.postId());
    }

    @Test
    void adminCanHideAndRestoreBlockedAuthorPostButUserDetailStillExcludesIt() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED, UserStatus.BLOCKED);
        authenticate(fixture.adminId(), UserRole.ADMIN, UserStatus.ACTIVE);

        assertThat(adminPostService.hidePost(fixture.postId(),
                new AdminHidePostRequest(AdminPostHideReason.SPAM)).status()).isEqualTo(PostStatus.HIDDEN);
        assertThat(adminPostService.restorePost(fixture.postId()).status()).isEqualTo(PostStatus.PUBLISHED);

        authenticate(fixture.viewerId(), UserRole.USER, UserStatus.ACTIVE);
        assertBusinessError(() -> postService.getPostDetail(fixture.postId()), ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void hideActionFailureRollsBackPublishedPostAndLeavesNoOrphanAction() {
        Fixture fixture = createFixture(PostStatus.PUBLISHED, UserStatus.ACTIVE);
        authenticate(fixture.adminId(), UserRole.ADMIN, UserStatus.ACTIVE);
        doThrow(new IllegalStateException("hide audit failed intentionally"))
                .when(actionRepository).save(any(AdminAction.class));

        assertThatThrownBy(() -> adminPostService.hidePost(
                fixture.postId(), new AdminHidePostRequest(AdminPostHideReason.SPAM)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(postState(fixture.postId())).isEqualTo("PUBLISHED|null|null|null");
        assertThat(actionRows(fixture.postId())).isEmpty();
    }

    @Test
    void restoreActionFailureRollsBackHiddenPostAndLeavesNoOrphanAction() {
        Fixture fixture = createFixture(PostStatus.HIDDEN, UserStatus.ACTIVE);
        authenticate(fixture.adminId(), UserRole.ADMIN, UserStatus.ACTIVE);
        doThrow(new IllegalStateException("restore audit failed intentionally"))
                .when(actionRepository).save(any(AdminAction.class));

        assertThatThrownBy(() -> adminPostService.restorePost(fixture.postId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(postState(fixture.postId())).isEqualTo(
                "HIDDEN|" + fixture.adminId() + "|" + timestamp(NOW.minusHours(1)) + "|SPAM");
        assertThat(actionRows(fixture.postId())).isEmpty();
    }

    @Test
    void twoConcurrentHidesProduceOneSuccessAndOneAction() throws Exception {
        Fixture fixture = createFixture(PostStatus.PUBLISHED, UserStatus.ACTIVE);
        assertConcurrentResult(fixture, true, ErrorCode.ADMIN_POST_ALREADY_HIDDEN);
        assertThat(actionRows(fixture.postId())).containsExactly("HIDE_POST|POST|SPAM|null|null");
    }

    @Test
    void twoConcurrentRestoresProduceOneSuccessAndOneAction() throws Exception {
        Fixture fixture = createFixture(PostStatus.HIDDEN, UserStatus.ACTIVE);
        assertConcurrentResult(fixture, false, ErrorCode.ADMIN_POST_ALREADY_PUBLISHED);
        assertThat(actionRows(fixture.postId())).containsExactly(
                "RESTORE_POST|POST|ADMIN_RESTORE|null|null");
    }

    private void assertConcurrentResult(Fixture fixture, boolean hide, ErrorCode secondError) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<String>> results = List.of(
                    executor.submit(() -> mutateConcurrently(fixture, hide, ready, start)),
                    executor.submit(() -> mutateConcurrently(fixture, hide, ready, start)));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(results.get(0).get(20, TimeUnit.SECONDS),
                    results.get(1).get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("SUCCESS", secondError.name());
        } finally {
            executor.shutdownNow();
        }
    }

    private String mutateConcurrently(Fixture fixture, boolean hide,
            CountDownLatch ready, CountDownLatch start) throws Exception {
        authenticate(fixture.adminId(), UserRole.ADMIN, UserStatus.ACTIVE);
        ready.countDown();
        start.await(10, TimeUnit.SECONDS);
        try {
            if (hide) {
                adminPostService.hidePost(fixture.postId(), new AdminHidePostRequest(AdminPostHideReason.SPAM));
            } else {
                adminPostService.restorePost(fixture.postId());
            }
            return "SUCCESS";
        } catch (BusinessException exception) {
            return exception.getErrorCode().name();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private Fixture createFixture(PostStatus postStatus, UserStatus authorStatus) {
        currentFixture = transactionTemplate.execute(status -> {
            String marker = UUID.randomUUID().toString().replace("-", "");
            User admin = saveUser("post-admin-" + marker + "@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
            User author = saveUser("post-author-" + marker + "@example.com", UserRole.USER, authorStatus);
            User viewer = saveUser("post-viewer-" + marker + "@example.com", UserRole.USER, UserStatus.ACTIVE);
            saveProfile(admin, "Admin " + marker);
            saveProfile(author, "Author " + marker);
            saveProfile(viewer, "Viewer " + marker);
            Post post = new Post(author, "Visible " + marker);
            post.setStatus(postStatus);
            if (postStatus == PostStatus.HIDDEN) {
                post.setHiddenBy(admin);
                post.setHiddenAt(NOW.minusHours(1));
                post.setHiddenReason("SPAM");
            }
            post = postRepository.saveAndFlush(post);
            return new Fixture(admin.getId(), author.getId(), viewer.getId(), post.getId());
        });
        return currentFixture;
    }

    private User saveUser(String email, UserRole role, UserStatus status) {
        User user = new User(email, "hash");
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setRole(role);
        user.setStatus(status);
        if (status == UserStatus.BLOCKED) {
            user.setBlockedAt(NOW.minusDays(1));
            user.setBlockedReason("SPAM");
        }
        return userRepository.saveAndFlush(user);
    }

    private void saveProfile(User user, String displayName) {
        UserProfile profile = new UserProfile(user);
        profile.setUsername("admin_post_" + user.getId());
        profile.setDisplayName(displayName);
        // Hồ sơ hoàn tất phải có ngày sinh để thỏa check constraint của schema MySQL thật.
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(NOW.minusDays(1));
        userProfileRepository.saveAndFlush(profile);
    }

    private void authenticate(Long userId, UserRole role, UserStatus status) {
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, role, status);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private String postState(Long postId) {
        return jdbcTemplate.queryForObject("""
                SELECT CONCAT(status, '|', COALESCE(CAST(hidden_by AS CHAR), 'null'), '|',
                              COALESCE(DATE_FORMAT(hidden_at, '%Y-%m-%dT%H:%i:%s'), 'null'), '|',
                              COALESCE(hidden_reason, 'null'))
                FROM posts WHERE id = ?
                """, String.class, postId);
    }

    private String timestamp(LocalDateTime value) {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private List<String> actionRows(Long postId) {
        return jdbcTemplate.query("""
                SELECT action_type, target_type, note, old_data, new_data
                FROM admin_actions WHERE target_type = 'POST' AND target_id = ? ORDER BY id
                """, (row, index) -> row.getString(1) + "|" + row.getString(2) + "|" + row.getString(3)
                        + "|" + row.getString(4) + "|" + row.getString(5), postId);
    }

    private void assertBusinessError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(errorCode);
    }

    private record Fixture(Long adminId, Long authorId, Long viewerId, Long postId) {
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        Clock fixedAdminPostClock() {
            return Clock.fixed(Instant.parse("2026-07-15T01:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }
}
