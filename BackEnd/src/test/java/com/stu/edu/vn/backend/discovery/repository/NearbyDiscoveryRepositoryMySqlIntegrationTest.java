package com.stu.edu.vn.backend.discovery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.discovery.cursor.NearbyCursor;
import com.stu.edu.vn.backend.discovery.model.NearbyBoundingBox;
import com.stu.edu.vn.backend.discovery.service.NearbyQuerySupport;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Integration test chỉ chạy trên MySQL test riêng đã áp dụng baseline canonical. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
@Import({NearbyDiscoveryRepository.class, NearbyQuerySupport.class})
class NearbyDiscoveryRepositoryMySqlIntegrationTest {
    private static final double QUERY_LATITUDE = 0.0d;
    private static final double QUERY_LONGITUDE = 0.0d;

    @Autowired private NearbyDiscoveryRepository repository;
    @Autowired private NearbyQuerySupport querySupport;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private NamedParameterJdbcTemplate namedJdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", NearbyDiscoveryRepositoryMySqlIntegrationTest::testDatabaseUrl);
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void filtersCandidateBlockBothDirectionsButDoesNotFilterRestrictOrDuplicateRepost() {
        long viewer = insertUser("USER", "ACTIVE", true);
        long visibleAuthor = insertUser("USER", "ACTIVE", true);
        long viewerBlockedAuthor = insertUser("USER", "ACTIVE", true);
        long authorBlockedViewer = insertUser("USER", "ACTIVE", true);
        long restrictedAuthor = insertUser("USER", "ACTIVE", true);
        long reverseRestrictedAuthor = insertUser("USER", "ACTIVE", true);
        long blockedAccountAuthor = insertUser("USER", "BLOCKED", true);
        long incompleteAuthor = insertUser("USER", "ACTIVE", false);
        long adminAuthor = insertUser("ADMIN", "ACTIVE", true);
        long nearLocation = insertLocation(0.0d, 0.001d);

        long visiblePost = insertPost(visibleAuthor, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long restrictedPost = insertPost(restrictedAuthor, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long reverseRestrictedPost = insertPost(
                reverseRestrictedAuthor, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long blockedForwardPost = insertPost(
                viewerBlockedAuthor, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long blockedReversePost = insertPost(
                authorBlockedViewer, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long blockedAccountPost = insertPost(
                blockedAccountAuthor, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long incompletePost = insertPost(incompleteAuthor, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long adminPost = insertPost(adminAuthor, nearLocation, "PUBLISHED", LocalDateTime.now(), null);
        long deletedPost = insertPost(visibleAuthor, nearLocation, "DELETED", LocalDateTime.now(), null);
        long noLocationPost = insertPost(visibleAuthor, null, "PUBLISHED", LocalDateTime.now(), null);

        jdbcTemplate.update("INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (?, ?)",
                viewer, viewerBlockedAuthor);
        jdbcTemplate.update("INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (?, ?)",
                authorBlockedViewer, viewer);
        jdbcTemplate.update("INSERT INTO user_restrictions(restrictor_id, restricted_id) VALUES (?, ?)",
                viewer, restrictedAuthor);
        jdbcTemplate.update("INSERT INTO user_restrictions(restrictor_id, restricted_id) VALUES (?, ?)",
                reverseRestrictedAuthor, viewer);
        jdbcTemplate.update("INSERT INTO post_reposts(user_id, post_id) VALUES (?, ?)", viewer, visiblePost);

        List<NearbyPostRank> result = find(viewer, 1, null, 50);

        assertThat(result).extracting(NearbyPostRank::postId)
                .containsExactlyInAnyOrder(visiblePost, restrictedPost, reverseRestrictedPost)
                .doesNotContain(blockedForwardPost, blockedReversePost, blockedAccountPost,
                        incompletePost, adminPost, deletedPost, noLocationPost);
        assertThat(result).filteredOn(rank -> rank.postId().equals(visiblePost)).hasSize(1);
    }

    @Test
    void appliesExactRadiusRoundedDistanceOrderingAndCompleteKeysetWithoutDuplicateOrMissing() {
        long viewer = insertUser("USER", "ACTIVE", true);
        long author = insertUser("USER", "ACTIVE", true);
        LocalDateTime tieTime = LocalDateTime.of(2026, 8, 13, 10, 0);
        long sameLocation = insertLocation(0.0d, 0.001d);
        long fartherLocation = insertLocation(0.0d, 0.005d);
        long justInsideLocation = insertLocation(0.0d, 0.0089d);
        long justOutsideLocation = insertLocation(0.0d, 0.0091d);

        long older = insertPost(author, sameLocation, "PUBLISHED", tieTime.minusMinutes(1), null);
        long lowerIdAtTie = insertPost(author, sameLocation, "PUBLISHED", tieTime, null);
        long higherIdAtTie = insertPost(author, sameLocation, "PUBLISHED", tieTime, null);
        long farther = insertPost(author, fartherLocation, "PUBLISHED", tieTime.plusMinutes(5), null);
        long justInside = insertPost(author, justInsideLocation, "PUBLISHED", tieTime, null);
        long justOutside = insertPost(author, justOutsideLocation, "PUBLISHED", tieTime, null);

        List<NearbyPostRank> all = find(viewer, 1, null, 20);
        assertThat(all).extracting(NearbyPostRank::postId)
                .containsSubsequence(higherIdAtTie, lowerIdAtTie, older, farther, justInside)
                .doesNotContain(justOutside);
        assertThat(all).extracting(NearbyPostRank::distanceMeters).isSorted();

        List<NearbyPostRank> firstPage = find(viewer, 1, null, 2);
        NearbyPostRank last = firstPage.getLast();
        NearbyCursor cursor = new NearbyCursor(
                NearbyCursor.CURRENT_VERSION,
                last.distanceMeters(),
                last.publishedAt(),
                last.postId(),
                querySupport.fingerprint(QUERY_LATITUDE, QUERY_LONGITUDE, 1));
        List<NearbyPostRank> secondPage = find(viewer, 1, cursor, 20);

        assertThat(secondPage).extracting(NearbyPostRank::postId)
                .doesNotContainAnyElementsOf(firstPage.stream().map(NearbyPostRank::postId).toList());
        assertThat(firstPage.stream().map(NearbyPostRank::postId).toList())
                .containsExactlyElementsOf(all.stream().limit(2).map(NearbyPostRank::postId).toList());
        assertThat(secondPage).extracting(NearbyPostRank::postId)
                .containsExactlyElementsOf(all.stream().skip(2).map(NearbyPostRank::postId).toList());
    }

    @Test
    void nativeQueryHasAValidMySql8ExecutionPlan() {
        long viewer = insertUser("USER", "ACTIVE", true);
        NearbyBoundingBox box = querySupport.boundingBox(QUERY_LATITUDE, QUERY_LONGITUDE, 5);
        MapSqlParameterSource parameters = parameters(viewer, 5, box, null, 11);

        String plan = namedJdbcTemplate.queryForObject(
                "EXPLAIN FORMAT=JSON " + NearbyDiscoveryRepository.FIND_NEARBY_SQL,
                parameters,
                String.class);

        assertThat(plan).isNotBlank().contains("query_block", "posts", "locations", "user_blocks");
    }

    private List<NearbyPostRank> find(long viewer, int radiusKm, NearbyCursor cursor, int limit) {
        NearbyBoundingBox box = querySupport.boundingBox(QUERY_LATITUDE, QUERY_LONGITUDE, radiusKm);
        return repository.findNearby(
                viewer, QUERY_LATITUDE, QUERY_LONGITUDE, radiusKm, box, cursor, limit);
    }

    private MapSqlParameterSource parameters(
            long viewer,
            int radiusKm,
            NearbyBoundingBox box,
            NearbyCursor cursor,
            int limit
    ) {
        boolean hasCursor = cursor != null;
        return new MapSqlParameterSource()
                .addValue("viewerId", viewer)
                .addValue("latitude", QUERY_LATITUDE)
                .addValue("longitude", QUERY_LONGITUDE)
                .addValue("radiusMeters", radiusKm * 1000L)
                .addValue("minimumLatitude", box.minimumLatitude())
                .addValue("maximumLatitude", box.maximumLatitude())
                .addValue("minimumLongitude", box.minimumLongitude())
                .addValue("maximumLongitude", box.maximumLongitude())
                .addValue("allLongitudes", box.allLongitudes() ? 1 : 0)
                .addValue("wrapsAntimeridian", box.wrapsAntimeridian() ? 1 : 0)
                .addValue("hasCursor", hasCursor ? 1 : 0)
                .addValue("cursorDistanceMeters", hasCursor ? cursor.distanceMeters() : 0L)
                .addValue("cursorPublishedAt", hasCursor
                        ? cursor.publishedAt()
                        : LocalDateTime.of(9999, 12, 31, 23, 59, 59))
                .addValue("cursorPostId", hasCursor ? cursor.postId() : Long.MAX_VALUE)
                .addValue("resultLimit", limit);
    }

    private long insertUser(String role, String status, boolean completed) {
        String marker = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO users(email, role, status, blocked_at)
                    VALUES (?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "nearby-" + marker + "@example.com");
            statement.setString(2, role);
            statement.setString(3, status);
            statement.setObject(4, "BLOCKED".equals(status) ? java.sql.Timestamp.valueOf(LocalDateTime.now()) : null);
            return statement;
        }, keyHolder);
        long userId = keyHolder.getKey().longValue();
        jdbcTemplate.update(
                """
                INSERT INTO user_profiles(user_id, username, display_name, date_of_birth, profile_completed_at)
                VALUES (?, ?, ?, '2000-01-01', ?)
                """,
                userId,
                "nearby_" + marker,
                "Nearby " + marker,
                completed ? java.sql.Timestamp.valueOf(LocalDateTime.now()) : null);
        return userId;
    }

    private long insertLocation(double latitude, double longitude) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO locations(google_place_id, display_name, latitude, longitude)
                    VALUES (?, 'Nearby integration location', ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "nearby-place-" + UUID.randomUUID());
            statement.setDouble(2, latitude);
            statement.setDouble(3, longitude);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long insertPost(
            long authorId,
            Long locationId,
            String status,
            LocalDateTime publishedAt,
            Long hiddenBy
    ) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO posts(author_id, location_id, content, status, published_at,
                                      hidden_by, hidden_at, deleted_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, authorId);
            statement.setObject(2, locationId);
            statement.setString(3, "nearby-post-" + UUID.randomUUID());
            statement.setString(4, status);
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(publishedAt));
            statement.setObject(6, hiddenBy);
            statement.setObject(7, "HIDDEN".equals(status) ? java.sql.Timestamp.valueOf(publishedAt) : null);
            statement.setObject(8, "DELETED".equals(status) ? java.sql.Timestamp.valueOf(publishedAt) : null);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
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
}
