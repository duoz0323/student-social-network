package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserBlockRepository;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration test MySQL thật chứng minh Block được lọc trước limit/cursor ở các query Post.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class UserBlockPostQueryMySqlIntegrationTest {

    private static final LocalDateTime FIRST_CURSOR_TIME = LocalDateTime.of(9999, 12, 31, 23, 59);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", UserBlockPostQueryMySqlIntegrationTest::testDatabaseUrl);
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void forYouFillsLimitAndKeepsCursorStableAfterFilteringBothBlockDirections() {
        Fixture fixture = fixture();
        userBlockRepository.insertIfAbsent(fixture.viewer().getId(), fixture.blockedAuthor().getId());

        List<Post> firstPage = forYou(
                fixture.viewer().getId(), Integer.MAX_VALUE, FIRST_CURSOR_TIME, Long.MAX_VALUE, 3);

        assertThat(firstPage).hasSize(3)
                .allMatch(post -> post.getAuthor().getId().equals(fixture.visibleAuthor().getId()));
        Post lastValidPost = firstPage.getLast();
        List<Post> secondPage = forYou(
                fixture.viewer().getId(),
                lastValidPost.getLikeCount() + lastValidPost.getCommentCount(),
                lastValidPost.getPublishedAt(),
                lastValidPost.getId(),
                3);
        assertThat(secondPage)
                .extracting(Post::getId)
                .doesNotContainAnyElementsOf(firstPage.stream().map(Post::getId).toList());

        userBlockRepository.deleteBlock(fixture.viewer().getId(), fixture.blockedAuthor().getId());
        userBlockRepository.insertIfAbsent(fixture.blockedAuthor().getId(), fixture.viewer().getId());
        assertThat(forYou(
                fixture.viewer().getId(), Integer.MAX_VALUE, FIRST_CURSOR_TIME, Long.MAX_VALUE, 10))
                .noneMatch(post -> post.getAuthor().getId().equals(fixture.blockedAuthor().getId()));

        userBlockRepository.deleteBlock(fixture.blockedAuthor().getId(), fixture.viewer().getId());
        assertThat(forYou(
                fixture.viewer().getId(), Integer.MAX_VALUE, FIRST_CURSOR_TIME, Long.MAX_VALUE, 10))
                .anyMatch(post -> post.getAuthor().getId().equals(fixture.blockedAuthor().getId()));
    }

    @Test
    void profileLikedSavedAndFollowingQueriesHideBlockedAuthorInsideDatabase() {
        Fixture fixture = fixture();
        Post blockedPost = fixture.blockedPosts().getFirst();
        Post visiblePost = fixture.visiblePosts().getFirst();
        jdbcTemplate.update("INSERT INTO post_likes(post_id, user_id) VALUES (?, ?)",
                blockedPost.getId(), fixture.viewer().getId());
        jdbcTemplate.update("INSERT INTO post_likes(post_id, user_id) VALUES (?, ?)",
                visiblePost.getId(), fixture.viewer().getId());
        jdbcTemplate.update("INSERT INTO saved_posts(user_id, post_id) VALUES (?, ?)",
                fixture.viewer().getId(), blockedPost.getId());
        jdbcTemplate.update("INSERT INTO saved_posts(user_id, post_id) VALUES (?, ?)",
                fixture.viewer().getId(), visiblePost.getId());
        jdbcTemplate.update("INSERT INTO follows(follower_id, following_id) VALUES (?, ?)",
                fixture.viewer().getId(), fixture.blockedAuthor().getId());
        jdbcTemplate.update("INSERT INTO follows(follower_id, following_id) VALUES (?, ?)",
                fixture.viewer().getId(), fixture.visibleAuthor().getId());
        userBlockRepository.insertIfAbsent(fixture.viewer().getId(), fixture.blockedAuthor().getId());

        assertThat(postRepository.findProfilePosts(
                fixture.blockedAuthor().getId(), fixture.viewer().getId(),
                FIRST_CURSOR_TIME, Long.MAX_VALUE, PageRequest.of(0, 10))).isEmpty();
        assertThat(postRepository.findLikedPosts(
                fixture.viewer().getId(), FIRST_CURSOR_TIME, Long.MAX_VALUE, PageRequest.of(0, 10)))
                .extracting(post -> post.getAuthor().getId())
                .containsExactly(fixture.visibleAuthor().getId());
        assertThat(postRepository.findSavedPosts(
                fixture.viewer().getId(), FIRST_CURSOR_TIME, Long.MAX_VALUE, PageRequest.of(0, 10)))
                .extracting(post -> post.getAuthor().getId())
                .containsExactly(fixture.visibleAuthor().getId());
        assertThat(postRepository.findFollowingFeed(
                fixture.viewer().getId(), FIRST_CURSOR_TIME, Long.MAX_VALUE, PageRequest.of(0, 10)))
                .allMatch(post -> post.getAuthor().getId().equals(fixture.visibleAuthor().getId()));
    }

    private List<Post> forYou(
            Long viewerId,
            int cursorScore,
            LocalDateTime cursorTime,
            Long cursorPostId,
            int limit
    ) {
        return postRepository.findForYouFeed(
                viewerId, cursorScore, cursorTime, cursorPostId, PageRequest.of(0, limit));
    }

    private Fixture fixture() {
        User viewer = completedUser("viewer");
        User blockedAuthor = completedUser("blocked");
        User visibleAuthor = completedUser("visible");
        List<Post> blockedPosts = List.of(createPost(blockedAuthor, "blocked-1"), createPost(blockedAuthor, "blocked-2"));
        List<Post> visiblePosts = List.of(
                createPost(visibleAuthor, "visible-1"),
                createPost(visibleAuthor, "visible-2"),
                createPost(visibleAuthor, "visible-3"),
                createPost(visibleAuthor, "visible-4")
        );
        return new Fixture(viewer, blockedAuthor, visibleAuthor, blockedPosts, visiblePosts);
    }

    private User completedUser(String marker) {
        String unique = marker + "-" + System.nanoTime();
        User newUser = new User(unique + "@example.com", "hash");
        // Fixture local có mật khẩu phải là email đã xác minh để thỏa CHECK canonical của bảng users.
        newUser.setEmailVerifiedAt(LocalDateTime.now());
        User user = userRepository.saveAndFlush(newUser);
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName(unique);
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(LocalDateTime.now());
        userProfileRepository.saveAndFlush(profile);
        return user;
    }

    private Post createPost(User author, String content) {
        return postRepository.saveAndFlush(new Post(author, content));
    }

    private static String testDatabaseUrl() {
        String url = requiredEnvironment("AUTH_TEST_DB_URL");
        if (!url.toLowerCase().contains("test")) {
            throw new IllegalStateException("AUTH_TEST_DB_URL phải trỏ tới database có tên chứa 'test'.");
        }
        return url;
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu biến môi trường " + name);
        }
        return value;
    }

    private record Fixture(
            User viewer,
            User blockedAuthor,
            User visibleAuthor,
            List<Post> blockedPosts,
            List<Post> visiblePosts
    ) {
    }
}
