package com.stu.edu.vn.backend.location.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LocationSchemaContractTest {

    @Test
    void baselineContainsLocationsAndOptionalPostForeignKey() throws Exception {
        // Kiểm tra source schema luôn chạy được ngay cả khi máy phát triển không có MySQL test riêng.
        String sql = Files.readString(databasePath("student_social_network.sql"));
        assertThat(sql)
                .contains("CREATE TABLE `locations`")
                .contains("`google_place_id` varchar(255) NOT NULL")
                .contains("UNIQUE KEY `uk_locations_google_place_id` (`google_place_id`)")
                .contains("CONSTRAINT `chk_locations_latitude`")
                .contains("CONSTRAINT `chk_locations_longitude`")
                .contains("`location_id` bigint unsigned DEFAULT NULL")
                .contains("KEY `idx_posts_location_id` (`location_id`)")
                .contains("CONSTRAINT `fk_posts_location` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT")
                .doesNotContain("UNIQUE KEY `uk_posts_location_id`");

        assertThat(sql.indexOf("CREATE TABLE `locations`")).isLessThan(sql.indexOf("CREATE TABLE `posts`"));
    }

    @Test
    void dbmlMatchesLocationSchemaAndRelationship() throws Exception {
        // DBML phải biểu diễn cùng nullable, unique, index và chính sách xóa như SQL MySQL.
        String dbml = Files.readString(databasePath("student_social_network.dbml"));
        assertThat(dbml)
                .contains("Table locations {")
                .contains("google_place_id varchar(255) [not null]")
                .contains("latitude decimal(10,7) [not null]")
                .contains("longitude decimal(10,7) [not null]")
                .contains("(google_place_id) [unique, name: \"uk_locations_google_place_id\"]")
                .contains("location_id bigint [note: \"UNSIGNED\"]")
                .contains("(location_id) [name: \"idx_posts_location_id\"]")
                .contains("Ref fk_posts_location: posts.location_id > locations.id [delete: set null, update: restrict]");
    }

    private Path databasePath(String first, String... more) {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        Path databaseDirectory = workingDirectory.resolve("database");
        if (!Files.isDirectory(databaseDirectory)) {
            databaseDirectory = workingDirectory.resolve("..").resolve("database").normalize();
        }
        return databaseDirectory.resolve(Path.of(first, more));
    }
}
