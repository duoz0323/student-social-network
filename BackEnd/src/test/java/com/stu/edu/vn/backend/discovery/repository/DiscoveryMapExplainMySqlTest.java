package com.stu.edu.vn.backend.discovery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Chỉ đọc EXPLAIN trên MySQL có dữ liệu; test này tuyệt đối không tạo hoặc sửa bản ghi. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "MAP_EXPLAIN_DB_URL", matches = "jdbc:mysql:.*")
class DiscoveryMapExplainMySqlTest {
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private NamedParameterJdbcTemplate namedJdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredEnvironment("MAP_EXPLAIN_DB_URL"));
        registry.add("spring.datasource.username", () -> requiredEnvironment("MAP_EXPLAIN_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("MAP_EXPLAIN_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void reportsTraditionalExplainColumnsWithoutMutatingDatabase() {
        Long viewerId = jdbcTemplate.queryForObject(
                """
                SELECT MIN(u.id)
                FROM users u
                JOIN user_profiles up ON up.user_id = u.id
                WHERE u.role = 'USER' AND u.status = 'ACTIVE' AND up.profile_completed_at IS NOT NULL
                """,
                Long.class);
        assertThat(viewerId).isNotNull();

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("viewerId", viewerId)
                .addValue("north", 90.0d)
                .addValue("south", -90.0d)
                .addValue("east", 180.0d)
                .addValue("west", -180.0d)
                .addValue("resultLimit", 201);
        List<Map<String, Object>> plan = namedJdbcTemplate.queryForList(
                "EXPLAIN " + DiscoveryMapRepository.FIND_MAP_LOCATIONS_SQL,
                parameters);

        assertThat(plan).isNotEmpty();
        for (Map<String, Object> row : plan) {
            System.out.printf(
                    "MAP_EXPLAIN table=%s type=%s possible_keys=%s key=%s rows=%s Extra=%s%n",
                    row.get("table"), row.get("type"), row.get("possible_keys"), row.get("key"),
                    row.get("rows"), row.get("Extra"));
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu biến môi trường " + name);
        }
        return value;
    }
}
