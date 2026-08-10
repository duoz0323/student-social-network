package com.stu.edu.vn.backend.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Xác minh hai HTTP request Block đồng thời vẫn idempotent trên MySQL thật.
 */
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = "bootstrap-admin.enabled=false")
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class UserBlockConcurrencyMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Fixture fixture;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @AfterEach
    void cleanUp() {
        if (fixture == null) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update(
                    "DELETE FROM notifications WHERE actor_id IN (?, ?, ?) OR recipient_id IN (?, ?, ?)",
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId(),
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId());
            jdbcTemplate.update(
                    "DELETE FROM user_blocks WHERE blocker_id IN (?, ?, ?) OR blocked_id IN (?, ?, ?)",
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId(),
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId());
            jdbcTemplate.update(
                    "DELETE FROM follows WHERE follower_id IN (?, ?, ?) OR following_id IN (?, ?, ?)",
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId(),
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId());
            if (fixture.postId() != null) {
                // Xóa Post trước User để cascade dọn Like/Comment fixture mà không ảnh hưởng dữ liệu test khác.
                jdbcTemplate.update("DELETE FROM posts WHERE id = ?", fixture.postId());
            }
            jdbcTemplate.update("DELETE FROM user_profiles WHERE user_id IN (?, ?, ?)",
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId());
            jdbcTemplate.update("DELETE FROM users WHERE id IN (?, ?, ?)",
                    fixture.blockerId(), fixture.targetId(), fixture.unrelatedId());
        });
    }

    @Test
    void twoConcurrentPutRequestsCreateOneBlockAndDeleteBothFollows() throws Exception {
        fixture = createFixture();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<Integer>> responses = List.of(
                    executor.submit(() -> blockRequest(ready, start)),
                    executor.submit(() -> blockRequest(ready, start))
            );

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            assertThat(List.of(
                    responses.get(0).get(20, TimeUnit.SECONDS),
                    responses.get(1).get(20, TimeUnit.SECONDS)))
                    .containsExactly(200, 200);
        } finally {
            start.countDown();
        }

        assertThat(count(
                "SELECT COUNT(*) FROM user_blocks WHERE blocker_id = ? AND blocked_id = ?"))
                .isEqualTo(1);
        assertThat(count("""
                SELECT COUNT(*) FROM follows
                WHERE (follower_id = ? AND following_id = ?)
                   OR (follower_id = ? AND following_id = ?)
                """, fixture.blockerId(), fixture.targetId(), fixture.targetId(), fixture.blockerId()))
                .isZero();
        assertThat(count("""
                SELECT COUNT(*) FROM notifications
                WHERE (actor_id = ? AND recipient_id = ?)
                   OR (actor_id = ? AND recipient_id = ?)
                """, fixture.blockerId(), fixture.targetId(), fixture.targetId(), fixture.blockerId()))
                .isZero();
    }

    @Test
    void historicalInteractionShouldRemainStoredButCommentShouldHideUntilUnblock() throws Exception {
        fixture = createHistoryFixture();
        var blockerAuthentication = authenticationFor(fixture.blockerId());
        String historicalComment = "Bình luận lịch sử phải được giữ nguyên";

        // Tạo Like và Comment bằng API thật trước thời điểm Block để kiểm chứng trọn luồng nghiệp vụ.
        mockMvc.perform(post("/api/v1/posts/{postId}/likes", fixture.postId())
                        .with(authentication(blockerAuthentication)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", fixture.postId())
                        .with(authentication(blockerAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + historicalComment + "\"}"))
                .andExpect(status().isCreated());

        assertHistoricalInteractionState(1, 1);

        mockMvc.perform(put("/api/v1/users/{targetUserId}/block", fixture.targetId())
                        .with(authentication(blockerAuthentication)))
                .andExpect(status().isOk());

        // Block chỉ xóa Follow; Like, Comment và bộ đếm lịch sử phải được bảo toàn.
        assertHistoricalInteractionState(1, 1);
        assertThat(count("""
                SELECT COUNT(*) FROM follows
                WHERE (follower_id = ? AND following_id = ?)
                   OR (follower_id = ? AND following_id = ?)
                """, fixture.blockerId(), fixture.targetId(), fixture.targetId(), fixture.blockerId()))
                .isZero();

        // Người Block không còn đọc bài/comment hoặc tạo và hủy tương tác với bài của đối phương.
        mockMvc.perform(get("/api/v1/posts/{postId}", fixture.postId())
                        .with(authentication(blockerAuthentication)))
                // Post Detail trả 404 có chủ đích để không tiết lộ bài bị Block vẫn tồn tại.
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/posts/{postId}/likes", fixture.postId())
                        .with(authentication(blockerAuthentication)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", fixture.postId())
                        .with(authentication(blockerAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Không được tạo\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", fixture.postId())
                        .with(authentication(blockerAuthentication)))
                .andExpect(status().isForbidden());

        // Dù B là chủ bài, Block hai chiều vẫn phải ẩn Comment lịch sử của A khỏi API người dùng.
        String ownerResponse = mockMvc.perform(get("/api/v1/posts/{postId}/comments", fixture.postId())
                        .with(authentication(authenticationFor(fixture.targetId()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(ownerResponse).doesNotContain(historicalComment);

        mockMvc.perform(delete("/api/v1/users/{targetUserId}/block", fixture.targetId())
                        .with(authentication(blockerAuthentication)))
                .andExpect(status().isOk());

        // Unblock không phục hồi Follow và cũng không làm thay đổi Like/Comment lịch sử.
        assertHistoricalInteractionState(1, 1);
        String ownerResponseAfterUnblock = mockMvc.perform(get("/api/v1/posts/{postId}/comments", fixture.postId())
                        .with(authentication(authenticationFor(fixture.targetId()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(ownerResponseAfterUnblock).contains(historicalComment);
        assertThat(count("""
                SELECT COUNT(*) FROM follows
                WHERE (follower_id = ? AND following_id = ?)
                   OR (follower_id = ? AND following_id = ?)
                """, fixture.blockerId(), fixture.targetId(), fixture.targetId(), fixture.blockerId()))
                .isZero();
    }

    @Test
    void commentQueriesShouldFilterBothBlockDirectionsAndKeepReplyTreeAndPaginationConsistent() throws Exception {
        fixture = createCommentVisibilityFixture();
        var blockerAuthentication = authenticationFor(fixture.blockerId());
        var ownerAuthentication = authenticationFor(fixture.targetId());
        var unrelatedAuthentication = authenticationFor(fixture.unrelatedId());

        mockMvc.perform(put("/api/v1/users/{targetUserId}/block", fixture.targetId())
                        .with(authentication(blockerAuthentication)))
                .andExpect(status().isOk());

        // B chỉ còn thấy root của C; root A và toàn bộ reply thuộc nhánh đó bị ẩn ngay tại query.
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", fixture.postId())
                        .param("page", "0")
                        .param("size", "1")
                        .with(authentication(ownerAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].commentId").value(fixture.visibleRootId()))
                .andExpect(jsonPath("$.data.content[0].replyCount").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        // Reply A dưới root C bị ẩn riêng, còn root C vẫn hiển thị và không sinh reply mồ côi.
        mockMvc.perform(get("/api/v1/comments/{parentCommentId}/replies", fixture.visibleRootId())
                        .with(authentication(ownerAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
        mockMvc.perform(get("/api/v1/comments/{parentCommentId}/replies", fixture.blockedRootId())
                        .with(authentication(ownerAuthentication)))
                .andExpect(status().isForbidden());

        // C không liên quan vẫn thấy cả hai root và các reply hợp lệ.
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", fixture.postId())
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(unrelatedAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].replyCount").value(1))
                .andExpect(jsonPath("$.data.content[1].replyCount").value(1));

        assertCommentVisibilityFixtureStillStored();

        mockMvc.perform(delete("/api/v1/users/{targetUserId}/block", fixture.targetId())
                        .with(authentication(blockerAuthentication)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", fixture.postId())
                        .param("size", "10")
                        .with(authentication(ownerAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mockMvc.perform(get("/api/v1/comments/{parentCommentId}/replies", fixture.visibleRootId())
                        .with(authentication(ownerAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // Kiểm tra chiều ngược B -> A cho cùng viewer B và cùng dữ liệu lịch sử.
        mockMvc.perform(put("/api/v1/users/{targetUserId}/block", fixture.blockerId())
                        .with(authentication(ownerAuthentication)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", fixture.postId())
                        .param("size", "10")
                        .with(authentication(ownerAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].commentId").value(fixture.visibleRootId()));

        assertCommentVisibilityFixtureStillStored();
    }

    private int blockRequest(CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await(10, TimeUnit.SECONDS);
        return mockMvc.perform(put("/api/v1/users/{targetUserId}/block", fixture.targetId())
                        .with(authentication(authenticationFor(fixture.blockerId()))))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    private Fixture createFixture() {
        return transactionTemplate.execute(status -> {
            String marker = UUID.randomUUID().toString().replace("-", "");
            User blocker = saveCompletedUser("blocker-" + marker + "@example.com");
            User target = saveCompletedUser("target-" + marker + "@example.com");
            jdbcTemplate.update("INSERT INTO follows(follower_id, following_id) VALUES (?, ?)",
                    blocker.getId(), target.getId());
            jdbcTemplate.update("INSERT INTO follows(follower_id, following_id) VALUES (?, ?)",
                    target.getId(), blocker.getId());
            return new Fixture(blocker.getId(), target.getId(), null, null, null, null);
        });
    }

    private Fixture createHistoryFixture() {
        return transactionTemplate.execute(status -> {
            Fixture baseFixture = createFixture();
            User targetReference = userRepository.getReferenceById(baseFixture.targetId());
            Post post = postRepository.saveAndFlush(
                    new Post(targetReference, "Bài viết dùng kiểm tra lịch sử User Block"));
            return new Fixture(baseFixture.blockerId(), baseFixture.targetId(), null, post.getId(), null, null);
        });
    }

    private Fixture createCommentVisibilityFixture() {
        return transactionTemplate.execute(status -> {
            Fixture baseFixture = createFixture();
            String marker = UUID.randomUUID().toString().replace("-", "");
            User unrelated = saveCompletedUser("unrelated-" + marker + "@example.com");
            Post post = postRepository.saveAndFlush(new Post(
                    userRepository.getReferenceById(baseFixture.targetId()),
                    "Bài viết kiểm tra lọc cây bình luận theo User Block"));

            jdbcTemplate.update("""
                    INSERT INTO comments(post_id, user_id, parent_comment_id, content, status)
                    VALUES (?, ?, NULL, 'root-a', 'PUBLISHED')
                    """, post.getId(), baseFixture.blockerId());
            Long blockedRootId = jdbcTemplate.queryForObject(
                    "SELECT id FROM comments WHERE post_id = ? AND content = 'root-a'", Long.class, post.getId());
            jdbcTemplate.update("""
                    INSERT INTO comments(post_id, user_id, parent_comment_id, content, status)
                    VALUES (?, ?, ?, 'reply-c-to-a', 'PUBLISHED')
                    """, post.getId(), unrelated.getId(), blockedRootId);
            jdbcTemplate.update("""
                    INSERT INTO comments(post_id, user_id, parent_comment_id, content, status)
                    VALUES (?, ?, NULL, 'root-c', 'PUBLISHED')
                    """, post.getId(), unrelated.getId());
            Long visibleRootId = jdbcTemplate.queryForObject(
                    "SELECT id FROM comments WHERE post_id = ? AND content = 'root-c'", Long.class, post.getId());
            jdbcTemplate.update("""
                    INSERT INTO comments(post_id, user_id, parent_comment_id, content, status)
                    VALUES (?, ?, ?, 'reply-a-to-c', 'PUBLISHED')
                    """, post.getId(), baseFixture.blockerId(), visibleRootId);

            return new Fixture(
                    baseFixture.blockerId(),
                    baseFixture.targetId(),
                    unrelated.getId(),
                    post.getId(),
                    blockedRootId,
                    visibleRootId
            );
        });
    }

    private User saveCompletedUser(String email) {
        User user = new User(email, "hash");
        user.setEmailVerifiedAt(LocalDateTime.now());
        user = userRepository.saveAndFlush(user);
        UserProfile profile = new UserProfile(user);
        profile.setUsername("block_user_" + user.getId());
        profile.setDisplayName(email.substring(0, email.indexOf('@')));
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(LocalDateTime.now());
        userProfileRepository.saveAndFlush(profile);
        return user;
    }

    private int count(String sql, Object... parameters) {
        Object[] actualParameters = parameters.length == 0
                ? new Object[]{fixture.blockerId(), fixture.targetId()}
                : parameters;
        return jdbcTemplate.queryForObject(sql, Integer.class, actualParameters);
    }

    private void assertHistoricalInteractionState(int expectedLikeCount, int expectedCommentCount) {
        assertThat(count(
                "SELECT COUNT(*) FROM post_likes WHERE user_id = ? AND post_id = ?",
                fixture.blockerId(), fixture.postId())).isEqualTo(expectedLikeCount);
        assertThat(count(
                "SELECT COUNT(*) FROM comments WHERE user_id = ? AND post_id = ? AND status = 'PUBLISHED'",
                fixture.blockerId(), fixture.postId())).isEqualTo(expectedCommentCount);
        assertThat(jdbcTemplate.queryForMap(
                "SELECT like_count, comment_count FROM posts WHERE id = ?", fixture.postId()))
                .containsEntry("like_count", Integer.toUnsignedLong(expectedLikeCount))
                .containsEntry("comment_count", Integer.toUnsignedLong(expectedCommentCount));
    }

    private void assertCommentVisibilityFixtureStillStored() {
        assertThat(count(
                "SELECT COUNT(*) FROM comments WHERE post_id = ? AND status = 'PUBLISHED'",
                fixture.postId())).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT comment_count FROM posts WHERE id = ?", Integer.class, fixture.postId()))
                .isEqualTo(4);
    }

    private UsernamePasswordAuthenticationToken authenticationFor(Long userId) {
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, UserRole.USER, UserStatus.ACTIVE);
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu biến môi trường " + name);
        }
        return value;
    }

    private record Fixture(
            Long blockerId,
            Long targetId,
            Long unrelatedId,
            Long postId,
            Long blockedRootId,
            Long visibleRootId
    ) {
    }
}
