package com.stu.edu.vn.backend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UserProfileUsernameSchemaContractTest {

    @Test
    void baselineAndDbmlContainUsernameUniquenessAndCompletionInvariant() throws Exception {
        String sql = Files.readString(databasePath("student_social_network.sql"));
        String dbml = Files.readString(databasePath("student_social_network.dbml"));

        assertThat(sql).contains("`username` varchar(30) DEFAULT NULL");
        assertThat(sql).contains("UNIQUE KEY `uq_user_profiles_username` (`username`)");
        assertThat(sql).contains("(`username` is not null)");
        assertThat(dbml).contains("username varchar(30)");
        assertThat(dbml).contains("(username) [unique, name: \"uq_user_profiles_username\"]");
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
