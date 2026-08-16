package com.stu.edu.vn.backend.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Bảo vệ hai artifact database canonical và dữ liệu demo 1.000 tài khoản trong file import duy nhất. */
class DemoSeedContractTest {

    @Test
    void databaseDirectoryKeepsCanonicalArtifactsAndVersionedAdditiveMigrations() throws Exception {
        Path database = databaseDirectory();
        List<String> files;
        try (var paths = Files.walk(database)) {
            files = paths.filter(Files::isRegularFile)
                    .map(database::relativize)
                    .map(Path::toString)
                    .map(path -> path.replace('\\', '/'))
                    .sorted()
                    .toList();
        }

        assertThat(files).contains("student_social_network.dbml", "student_social_network.sql",
                "collaborator_managed_social_identity_20260815.sql",
                "collaborator_role_lifecycle_20260815.sql",
                "managed_profile_completion_hotfix_20260815.sql");
        assertThat(files).allMatch(file -> file.endsWith(".sql") || file.endsWith(".dbml"));
    }

    @Test
    void demoSeedCoversUsersAcademicProfilesPostsAndSelfVerification() throws Exception {
        String seed = Files.readString(databaseDirectory().resolve("student_social_network.sql"));

        assertThat(seed)
                .contains("WHILE user_no <= 1000 DO")
                .contains("WHILE post_no <= 1000 DO")
                .contains("TRUNCATE TABLE `user_interests`")
                .contains("`school_id`, `faculty_id`, `major_id`, `entry_year`")
                .contains("INSERT INTO `user_interests`")
                .contains("'invalid_demo_counts'")
                .contains("'invalid_academic_hierarchy'")
                .contains("'counter_mismatch'");

        // Canonical schema phải đặt account_type đúng bảng users và bảo vệ ngày sinh theo loại tài khoản.
        assertThat(seed)
                .contains("CREATE TRIGGER `trg_user_profiles_completion_birth_insert`")
                .contains("owner_account_type <> 'MANAGED'")
                .doesNotContain("`status` varchar(16) NOT NULL DEFAULT 'ACTIVE',\n  `account_type` varchar(16) NOT NULL DEFAULT 'NORMAL',\n  `expires_at`");
    }

    private Path databaseDirectory() {
        Path current = Path.of("").toAbsolutePath().normalize();
        return Files.isDirectory(current.resolve("database"))
                ? current.resolve("database")
                : current.resolve("..").resolve("database").normalize();
    }
}
