package com.stu.edu.vn.backend.analytics.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UserActivityAnalyticsSchemaContractTest {

    @Test
    void baselineAndDbmlContainMatchingActivitySchema() throws Exception {
        String sql = Files.readString(databasePath("student_social_network.sql"));
        String dbml = Files.readString(databasePath("student_social_network.dbml"));

        assertThat(sql)
                .contains("`first_active_at` datetime(6) DEFAULT NULL")
                .contains("`last_active_at` datetime(6) DEFAULT NULL")
                .contains("CREATE TABLE `user_daily_activities`")
                .contains("UNIQUE KEY `uq_user_daily_activities_user_date` (`user_id`,`activity_date`)")
                .contains("KEY `idx_user_daily_activities_date_user` (`activity_date`,`user_id`)")
                .contains("CONSTRAINT `fk_user_daily_activities_user`");
        assertThat(dbml)
                .contains("Table user_daily_activities {")
                .contains("(user_id, activity_date) [unique, name: \"uq_user_daily_activities_user_date\"]")
                .contains("Ref fk_user_daily_activities_user: user_daily_activities.user_id > users.id");
    }

    @Test
    void additiveMigrationUsesAtomicUniquenessWithoutDuplicateIndex() throws Exception {
        String migration = Files.readString(databasePath("migrations", "V005__add_user_activity_analytics.sql"));

        assertThat(migration)
                .contains("ADD COLUMN first_active_at")
                .contains("ADD COLUMN last_active_at")
                .contains("UNIQUE KEY uq_user_daily_activities_user_date (user_id, activity_date)")
                .contains("KEY idx_user_daily_activities_date_user (activity_date, user_id)")
                .doesNotContain("DROP TABLE", "DROP COLUMN");
    }

    private Path databasePath(String first, String... more) {
        Path current = Path.of("").toAbsolutePath().normalize();
        Path database = Files.isDirectory(current.resolve("database"))
                ? current.resolve("database")
                : current.resolve("..").resolve("database").normalize();
        return database.resolve(Path.of(first, more));
    }
}
