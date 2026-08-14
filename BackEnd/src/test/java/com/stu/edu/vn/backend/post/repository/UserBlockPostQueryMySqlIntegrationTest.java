package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.feed.repository.PersonalizedFeedRepository;
import com.stu.edu.vn.backend.feed.repository.projection.PersonalizedPostRankProjection;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserBlockRepository;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.data.jpa.repository.Query;
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
    private PersonalizedFeedRepository personalizedFeedRepository;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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

        LocalDateTime rankingAt = LocalDateTime.now();
        List<PersonalizedPostRankProjection> firstPage = forYou(
                fixture.viewer().getId(), rankingAt, Integer.MAX_VALUE, FIRST_CURSOR_TIME, Long.MAX_VALUE, 3);

        assertThat(firstPage).hasSize(3)
                .extracting(PersonalizedPostRankProjection::getPostId)
                .allMatch(fixture.visiblePosts().stream().map(Post::getId).toList()::contains);
        PersonalizedPostRankProjection lastValidPost = firstPage.getLast();
        List<PersonalizedPostRankProjection> secondPage = forYou(
                fixture.viewer().getId(),
                rankingAt,
                lastValidPost.getScore(),
                lastValidPost.getPublishedAt(),
                lastValidPost.getPostId(),
                3);
        assertThat(secondPage)
                .extracting(PersonalizedPostRankProjection::getPostId)
                .doesNotContainAnyElementsOf(firstPage.stream()
                        .map(PersonalizedPostRankProjection::getPostId).toList());

        userBlockRepository.deleteBlock(fixture.viewer().getId(), fixture.blockedAuthor().getId());
        userBlockRepository.insertIfAbsent(fixture.blockedAuthor().getId(), fixture.viewer().getId());
        assertThat(forYou(
                fixture.viewer().getId(), rankingAt, Integer.MAX_VALUE, FIRST_CURSOR_TIME, Long.MAX_VALUE, 10))
                .extracting(PersonalizedPostRankProjection::getPostId)
                .doesNotContainAnyElementsOf(fixture.blockedPosts().stream().map(Post::getId).toList());

        userBlockRepository.deleteBlock(fixture.blockedAuthor().getId(), fixture.viewer().getId());
        assertThat(forYou(
                fixture.viewer().getId(), rankingAt, Integer.MAX_VALUE, FIRST_CURSOR_TIME, Long.MAX_VALUE, 10))
                .extracting(PersonalizedPostRankProjection::getPostId)
                .containsAnyElementsOf(fixture.blockedPosts().stream().map(Post::getId).toList());
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

    @Test
    void personalizedSignalsAreAdditiveDynamicAndDoNotCountTheCandidateAsHistory() {
        LocalDateTime rankingAt = LocalDateTime.of(2026, 8, 11, 3, 0);
        User viewer = completedUser("personalized-viewer");
        User affinityAuthor = completedUser("personalized-affinity");
        User baselineAuthor = completedUser("personalized-baseline");
        Post affinityCandidate = createPost(affinityAuthor, "affinity-candidate");
        Post baselineCandidate = createPost(baselineAuthor, "baseline-candidate");
        Post affinityHistory = createPost(affinityAuthor, "affinity-history");
        jdbcTemplate.update("UPDATE posts SET published_at = ? WHERE id IN (?, ?)",
                rankingAt.minusHours(2), affinityCandidate.getId(), baselineCandidate.getId());
        jdbcTemplate.update("UPDATE posts SET published_at = ? WHERE id = ?",
                rankingAt.minusDays(10), affinityHistory.getId());

        Map<String, Object> academic = jdbcTemplate.queryForMap("""
                SELECT school.id AS school_id, faculty.id AS faculty_id, major.id AS major_id
                FROM majors major
                JOIN faculties faculty ON faculty.id = major.faculty_id
                JOIN schools school ON school.id = faculty.school_id
                WHERE school.status = 'ACTIVE' AND faculty.status = 'ACTIVE' AND major.status = 'ACTIVE'
                LIMIT 1
                """);
        Long schoolId = ((Number) academic.get("school_id")).longValue();
        Long facultyId = ((Number) academic.get("faculty_id")).longValue();
        Long majorId = ((Number) academic.get("major_id")).longValue();
        jdbcTemplate.update("""
                UPDATE user_profiles
                SET school_id = ?, faculty_id = ?, major_id = ?, entry_year = 2022
                WHERE user_id IN (?, ?)
                """, schoolId, facultyId, majorId, viewer.getId(), affinityAuthor.getId());

        Long interestId = jdbcTemplate.queryForObject(
                "SELECT id FROM interest_categories WHERE status = 'ACTIVE' ORDER BY id LIMIT 1", Long.class);
        jdbcTemplate.update("INSERT INTO user_interests(user_id, interest_id) VALUES (?, ?), (?, ?)",
                viewer.getId(), interestId, affinityAuthor.getId(), interestId);
        jdbcTemplate.update("INSERT INTO follows(follower_id, following_id) VALUES (?, ?)",
                viewer.getId(), affinityAuthor.getId());

        String marker = "personalized_" + System.nanoTime();
        jdbcTemplate.update("INSERT INTO hashtags(normalized_name, display_name) VALUES (?, ?)", marker, marker);
        Long sharedHashtagId = jdbcTemplate.queryForObject(
                "SELECT id FROM hashtags WHERE normalized_name = ?", Long.class, marker);
        jdbcTemplate.update("INSERT INTO post_hashtags(post_id, hashtag_id) VALUES (?, ?), (?, ?)",
                affinityCandidate.getId(), sharedHashtagId, affinityHistory.getId(), sharedHashtagId);
        addAllInteractions(viewer.getId(), affinityHistory.getId());

        String candidateOnlyMarker = marker + "_candidate";
        jdbcTemplate.update("INSERT INTO hashtags(normalized_name, display_name) VALUES (?, ?)",
                candidateOnlyMarker, candidateOnlyMarker);
        Long candidateOnlyHashtagId = jdbcTemplate.queryForObject(
                "SELECT id FROM hashtags WHERE normalized_name = ?", Long.class, candidateOnlyMarker);
        jdbcTemplate.update("INSERT INTO post_hashtags(post_id, hashtag_id) VALUES (?, ?)",
                baselineCandidate.getId(), candidateOnlyHashtagId);
        addAllInteractions(viewer.getId(), baselineCandidate.getId());

        Map<Long, Integer> initialScores = scoreByPostId(viewer.getId(), rankingAt);
        assertThat(initialScores.get(affinityCandidate.getId())).isEqualTo(150);
        // Interaction trên chính candidate chỉ tăng engagement 1 Like + 2 Comment + 2 Repost, không tăng history/hashtag.
        assertThat(initialScores.get(baselineCandidate.getId())).isEqualTo(65);

        jdbcTemplate.update("UPDATE schools SET status = 'INACTIVE' WHERE id = ?", schoolId);
        jdbcTemplate.update("UPDATE faculties SET status = 'INACTIVE' WHERE id = ?", facultyId);
        jdbcTemplate.update("UPDATE majors SET status = 'INACTIVE' WHERE id = ?", majorId);
        jdbcTemplate.update("UPDATE interest_categories SET status = 'INACTIVE' WHERE id = ?", interestId);
        assertThat(scoreByPostId(viewer.getId(), rankingAt).get(affinityCandidate.getId())).isEqualTo(123);

        jdbcTemplate.update("DELETE FROM follows WHERE follower_id = ? AND following_id = ?",
                viewer.getId(), affinityAuthor.getId());
        jdbcTemplate.update("INSERT INTO user_restrictions(restrictor_id, restricted_id) VALUES (?, ?)",
                viewer.getId(), affinityAuthor.getId());
        assertThat(scoreByPostId(viewer.getId(), rankingAt).get(affinityCandidate.getId())).isEqualTo(93);

        userBlockRepository.insertIfAbsent(viewer.getId(), affinityAuthor.getId());
        assertThat(scoreByPostId(viewer.getId(), rankingAt)).doesNotContainKey(affinityCandidate.getId());
    }

    @Test
    void personalizedQueryHasAValidMySql8ExecutionPlan() throws Exception {
        User viewer = completedUser("personalized-explain");
        String query = PersonalizedFeedRepository.class.getMethod(
                        "findRankedPosts", Long.class, LocalDateTime.class, int.class,
                        LocalDateTime.class, Long.class, org.springframework.data.domain.Pageable.class)
                .getAnnotation(Query.class).value();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("viewerId", viewer.getId())
                .addValue("rankingAt", LocalDateTime.of(2026, 8, 11, 3, 0))
                .addValue("cursorScore", Integer.MAX_VALUE)
                .addValue("cursorPublishedAt", FIRST_CURSOR_TIME)
                .addValue("cursorPostId", Long.MAX_VALUE);

        String plan = namedParameterJdbcTemplate.queryForObject(
                "EXPLAIN FORMAT=JSON " + query + " LIMIT 11", parameters, String.class);

        assertThat(plan).isNotBlank().contains("query_block", "user_blocks", "post_likes", "saved_posts");
    }

    private List<PersonalizedPostRankProjection> forYou(
            Long viewerId,
            LocalDateTime rankingAt,
            int cursorScore,
            LocalDateTime cursorTime,
            Long cursorPostId,
            int limit
    ) {
        return personalizedFeedRepository.findRankedPosts(
                viewerId, rankingAt, cursorScore, cursorTime, cursorPostId, PageRequest.of(0, limit));
    }

    private Map<Long, Integer> scoreByPostId(Long viewerId, LocalDateTime rankingAt) {
        return forYou(viewerId, rankingAt, Integer.MAX_VALUE, FIRST_CURSOR_TIME, Long.MAX_VALUE, 2000).stream()
                .collect(Collectors.toMap(
                        PersonalizedPostRankProjection::getPostId,
                        PersonalizedPostRankProjection::getScore,
                        (left, right) -> left
                ));
    }

    private void addAllInteractions(Long viewerId, Long postId) {
        jdbcTemplate.update("INSERT INTO post_likes(user_id, post_id) VALUES (?, ?)", viewerId, postId);
        jdbcTemplate.update("INSERT INTO comments(post_id, user_id, content) VALUES (?, ?, 'test')", postId, viewerId);
        jdbcTemplate.update("INSERT INTO saved_posts(user_id, post_id) VALUES (?, ?)", viewerId, postId);
        jdbcTemplate.update("INSERT INTO post_reposts(user_id, post_id) VALUES (?, ?)", viewerId, postId);
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
        profile.setUsername("post_user_" + user.getId());
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
