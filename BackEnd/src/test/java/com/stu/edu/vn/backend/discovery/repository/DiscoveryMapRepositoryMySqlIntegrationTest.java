package com.stu.edu.vn.backend.discovery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.discovery.cursor.MapLocationPostsCursor;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationResponse;
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

/** Kiểm chứng native Map query trên MySQL test đã áp dụng schema canonical. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
@Import(DiscoveryMapRepository.class)
class DiscoveryMapRepositoryMySqlIntegrationTest {
    @Autowired private DiscoveryMapRepository repository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private NamedParameterJdbcTemplate namedJdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DiscoveryMapRepositoryMySqlIntegrationTest::testDatabaseUrl);
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void markerAggregatesVisiblePostsAndKeepsRestrictWithoutRepostDuplication() {
        long viewer = insertUser("USER", "ACTIVE", true);
        long visibleAuthor = insertUser("USER", "ACTIVE", true);
        long viewerBlockedAuthor = insertUser("USER", "ACTIVE", true);
        long authorBlockedViewer = insertUser("USER", "ACTIVE", true);
        long restrictedAuthor = insertUser("USER", "ACTIVE", true);
        long blockedAuthor = insertUser("USER", "BLOCKED", true);
        long incompleteAuthor = insertUser("USER", "ACTIVE", false);
        long adminAuthor = insertUser("ADMIN", "ACTIVE", true);
        long inside = insertLocation(0.5d, 0.5d, "Inside");
        long boundary = insertLocation(1.0d, 1.0d, "Boundary");
        long outside = insertLocation(1.1d, 1.1d, "Outside");
        LocalDateTime base = LocalDateTime.of(2026, 8, 14, 1, 0);

        long olderVisible = insertPost(visibleAuthor, inside, "PUBLISHED", base, null);
        long latestVisible = insertPost(visibleAuthor, inside, "PUBLISHED", base.plusMinutes(20), null);
        insertPost(restrictedAuthor, inside, "PUBLISHED", base.plusMinutes(10), null);
        insertPost(viewerBlockedAuthor, inside, "PUBLISHED", base.plusMinutes(30), null);
        insertPost(authorBlockedViewer, inside, "PUBLISHED", base.plusMinutes(30), null);
        insertPost(blockedAuthor, inside, "PUBLISHED", base.plusMinutes(30), null);
        insertPost(incompleteAuthor, inside, "PUBLISHED", base.plusMinutes(30), null);
        insertPost(adminAuthor, inside, "PUBLISHED", base.plusMinutes(30), null);
        insertPost(visibleAuthor, inside, "HIDDEN", base.plusMinutes(30), visibleAuthor);
        insertPost(visibleAuthor, inside, "DELETED", base.plusMinutes(30), null);
        insertPost(visibleAuthor, null, "PUBLISHED", base.plusMinutes(30), null);
        insertPost(visibleAuthor, boundary, "PUBLISHED", base.plusMinutes(5), null);
        insertPost(visibleAuthor, outside, "PUBLISHED", base.plusMinutes(40), null);

        jdbcTemplate.update("INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (?, ?)",
                viewer, viewerBlockedAuthor);
        jdbcTemplate.update("INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (?, ?)",
                authorBlockedViewer, viewer);
        jdbcTemplate.update("INSERT INTO user_restrictions(restrictor_id, restricted_id) VALUES (?, ?)",
                viewer, restrictedAuthor);
        jdbcTemplate.update("INSERT INTO post_reposts(user_id, post_id) VALUES (?, ?)", viewer, olderVisible);
        jdbcTemplate.update("INSERT INTO post_reposts(user_id, post_id) VALUES (?, ?)", restrictedAuthor, latestVisible);

        List<MapLocationResponse> markers = repository.findMapLocations(viewer, 1.0d, 0.0d, 1.0d, 0.0d, 201);

        assertThat(markers).extracting(MapLocationResponse::locationId).containsExactly(inside, boundary);
        MapLocationResponse insideMarker = markers.getFirst();
        assertThat(insideMarker.postCount()).isEqualTo(3L);
        assertThat(insideMarker.latestPostAt()).isEqualTo(base.plusMinutes(20));
    }

    @Test
    void locationPostsUsePublishedAtAndIdKeysetWithoutDuplicate() {
        long viewer = insertUser("USER", "ACTIVE", true);
        long author = insertUser("USER", "ACTIVE", true);
        long restrictedAuthor = insertUser("USER", "ACTIVE", true);
        long location = insertLocation(0.5d, 0.5d, "Keyset");
        long otherLocation = insertLocation(0.6d, 0.6d, "Other");
        LocalDateTime tie = LocalDateTime.of(2026, 8, 14, 1, 20);
        long older = insertPost(author, location, "PUBLISHED", tie.minusMinutes(1), null);
        long lowerTie = insertPost(author, location, "PUBLISHED", tie, null);
        long higherTie = insertPost(restrictedAuthor, location, "PUBLISHED", tie, null);
        insertPost(author, otherLocation, "PUBLISHED", tie.plusMinutes(1), null);
        insertPost(author, location, "HIDDEN", tie.plusMinutes(2), author);
        jdbcTemplate.update("INSERT INTO user_restrictions(restrictor_id, restricted_id) VALUES (?, ?)",
                viewer, restrictedAuthor);
        jdbcTemplate.update("INSERT INTO post_reposts(user_id, post_id) VALUES (?, ?)", viewer, higherTie);

        List<MapLocationPostKey> all = repository.findLocationPostKeys(viewer, location, null, 20);
        assertThat(all).extracting(MapLocationPostKey::postId).containsExactly(higherTie, lowerTie, older);

        List<MapLocationPostKey> firstPage = repository.findLocationPostKeys(viewer, location, null, 2);
        MapLocationPostKey last = firstPage.getLast();
        MapLocationPostsCursor cursor = new MapLocationPostsCursor(
                1, location, last.publishedAt(), last.postId());
        List<MapLocationPostKey> secondPage = repository.findLocationPostKeys(viewer, location, cursor, 20);

        assertThat(firstPage).extracting(MapLocationPostKey::postId).containsExactly(higherTie, lowerTie);
        assertThat(secondPage).extracting(MapLocationPostKey::postId).containsExactly(older);
    }

    @Test
    void markerAggregationHasValidMySql8ExecutionPlan() {
        long viewer = insertUser("USER", "ACTIVE", true);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("viewerId", viewer)
                .addValue("north", 1.0d)
                .addValue("south", 0.0d)
                .addValue("east", 1.0d)
                .addValue("west", 0.0d)
                .addValue("resultLimit", 201);

        String plan = namedJdbcTemplate.queryForObject(
                "EXPLAIN FORMAT=JSON " + DiscoveryMapRepository.FIND_MAP_LOCATIONS_SQL,
                parameters,
                String.class);

        assertThat(plan).isNotBlank().contains("query_block", "posts", "locations", "user_blocks");
    }

    private long insertUser(String role, String status, boolean completed) {
        String marker = marker();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users(email, role, status, blocked_at) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "map-" + marker + "@example.com");
            statement.setString(2, role);
            statement.setString(3, status);
            statement.setObject(4, "BLOCKED".equals(status)
                    ? java.sql.Timestamp.valueOf(LocalDateTime.now()) : null);
            return statement;
        }, keyHolder);
        long userId = keyHolder.getKey().longValue();
        jdbcTemplate.update(
                """
                INSERT INTO user_profiles(user_id, username, display_name, date_of_birth, profile_completed_at)
                VALUES (?, ?, ?, '2000-01-01', ?)
                """,
                userId, "map_" + marker, "Map " + marker,
                completed ? java.sql.Timestamp.valueOf(LocalDateTime.now()) : null);
        return userId;
    }

    private long insertLocation(double latitude, double longitude, String displayName) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO locations(google_place_id, display_name, formatted_address, latitude, longitude)
                    VALUES (?, ?, 'Map integration address', ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "map-place-" + UUID.randomUUID());
            statement.setString(2, displayName);
            statement.setDouble(3, latitude);
            statement.setDouble(4, longitude);
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
            statement.setString(3, "map-post-" + UUID.randomUUID());
            statement.setString(4, status);
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(publishedAt));
            statement.setObject(6, hiddenBy);
            statement.setObject(7, "HIDDEN".equals(status)
                    ? java.sql.Timestamp.valueOf(publishedAt) : null);
            statement.setObject(8, "DELETED".equals(status)
                    ? java.sql.Timestamp.valueOf(publishedAt) : null);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private String marker() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
