package com.stu.edu.vn.backend.location.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration test chỉ chạy trên MySQL test riêng đã áp dụng baseline hoặc migration Location.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LOCATION_TEST_DB_URL", matches = "jdbc:mysql:.*")
class LocationRepositoryMySqlIntegrationTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("LOCATION_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("LOCATION_TEST_DB_USERNAME"));
        registry.add(
                "spring.datasource.password",
                () -> System.getenv().getOrDefault("LOCATION_TEST_DB_PASSWORD", "")
        );
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void savesAndFindsLocationByGooglePlaceId() {
        Location location = locationRepository.saveAndFlush(newLocation(uniquePlaceId()));

        assertThat(locationRepository.findByGooglePlaceId(location.getGooglePlaceId()))
                .hasValueSatisfying(found -> assertThat(found.getId()).isEqualTo(location.getId()));
        assertThat(locationRepository.existsByGooglePlaceId(location.getGooglePlaceId())).isTrue();
    }

    @Test
    void databaseRejectsDuplicateGooglePlaceId() {
        String placeId = uniquePlaceId();
        locationRepository.saveAndFlush(newLocation(placeId));

        assertThatThrownBy(() -> locationRepository.saveAndFlush(newLocation(placeId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void multiplePostsCanShareLocationAndPostCanRemainWithoutLocation() {
        User author = saveAuthor();
        Location location = locationRepository.saveAndFlush(newLocation(uniquePlaceId()));
        Post first = new Post(author, "Bài viết thứ nhất dùng chung địa điểm");
        first.setLocation(location);
        Post second = new Post(author, "Bài viết thứ hai dùng chung địa điểm");
        second.setLocation(location);
        Post withoutLocation = new Post(author, "Bài viết không gắn địa điểm");

        postRepository.saveAllAndFlush(java.util.List.of(first, second, withoutLocation));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts WHERE location_id = ?",
                Integer.class,
                location.getId()
        )).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT location_id FROM posts WHERE id = ?",
                Long.class,
                withoutLocation.getId()
        )).isNull();
    }

    @Test
    void deletingPostDoesNotDeleteSharedLocation() {
        User author = saveAuthor();
        Location location = locationRepository.saveAndFlush(newLocation(uniquePlaceId()));
        Post post = new Post(author, "Bài viết sẽ bị xóa cứng trong integration test");
        post.setLocation(location);
        postRepository.saveAndFlush(post);

        postRepository.delete(post);
        postRepository.flush();

        assertThat(locationRepository.existsById(location.getId())).isTrue();
    }

    @Test
    void detachingLocationOnlySetsPostForeignKeyToNull() {
        User author = saveAuthor();
        Location location = locationRepository.saveAndFlush(newLocation(uniquePlaceId()));
        Post post = new Post(author, "Bài viết sẽ gỡ địa điểm");
        post.setLocation(location);
        postRepository.saveAndFlush(post);

        post.setLocation(null);
        postRepository.saveAndFlush(post);
        entityManager.clear();

        assertThat(postRepository.findById(post.getId()).orElseThrow().getLocation()).isNull();
        assertThat(locationRepository.existsById(location.getId())).isTrue();
    }

    @Test
    void deletingLocationSetsReferencingPostForeignKeyToNull() {
        User author = saveAuthor();
        Location location = locationRepository.saveAndFlush(newLocation(uniquePlaceId()));
        Post post = new Post(author, "Bài viết kiểm tra ON DELETE SET NULL");
        post.setLocation(location);
        postRepository.saveAndFlush(post);
        entityManager.clear();

        locationRepository.deleteById(location.getId());
        locationRepository.flush();
        entityManager.clear();

        assertThat(postRepository.findById(post.getId()).orElseThrow().getLocation()).isNull();
    }

    @Test
    void databaseRejectsCoordinatesOutsideAllowedRanges() {
        assertThatThrownBy(() -> insertRawLocation(new BigDecimal("90.0000001"), BigDecimal.ZERO))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsLongitudeOutsideAllowedRange() {
        assertThatThrownBy(() -> insertRawLocation(BigDecimal.ZERO, new BigDecimal("180.0000001")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User saveAuthor() {
        return userRepository.saveAndFlush(new User(
                "location-test-" + UUID.randomUUID() + "@example.com",
                "bcrypt-test-hash"
        ));
    }

    private Location newLocation(String placeId) {
        return new Location(
                placeId,
                "Đại học Công nghệ Sài Gòn",
                "180 Cao Lỗ, Quận 8, TP.HCM",
                new BigDecimal("10.7382456"),
                new BigDecimal("106.6778123")
        );
    }

    private String uniquePlaceId() {
        return "ChIJ-location-test-" + UUID.randomUUID();
    }

    private void insertRawLocation(BigDecimal latitude, BigDecimal longitude) {
        jdbcTemplate.update(
                """
                INSERT INTO locations (google_place_id, display_name, latitude, longitude)
                VALUES (?, ?, ?, ?)
                """,
                uniquePlaceId(),
                "Địa điểm kiểm tra constraint",
                latitude,
                longitude
        );
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " phải trỏ tới MySQL dành riêng cho integration test");
        }
        return value;
    }
}
