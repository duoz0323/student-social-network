package com.stu.edu.vn.backend.academic.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import com.stu.edu.vn.backend.academic.entity.Faculty;
import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.entity.School;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Bảo vệ tính đồng bộ giữa SQL canonical, DBML và JPA mapping. */
class AcademicSchemaContractTest {
    @Test
    void sqlAndDbmlContainSynchronizedAcademicSchema() throws Exception {
        String sql = Files.readString(databasePath("student_social_network.sql"));
        String dbml = Files.readString(databasePath("student_social_network.dbml"));

        for (String table : java.util.List.of(
                "schools", "faculties", "majors", "interest_categories", "user_interests")) {
            assertThat(sql).contains("CREATE TABLE `" + table + "`");
            assertThat(dbml).contains("Table " + table + " {");
        }
        assertThat(sql).contains("`school_id` bigint unsigned DEFAULT NULL");
        assertThat(sql).contains("`faculty_id` bigint unsigned DEFAULT NULL");
        assertThat(sql).contains("`major_id` bigint unsigned DEFAULT NULL");
        assertThat(sql).contains("PRIMARY KEY (`user_id`,`interest_id`)");
        assertThat(dbml).contains("entry_year smallint");
        assertThat(sql).contains("chk_user_profiles_entry_year");
    }

    @Test
    void entityMappingsMatchAcademicTablesAndJoinColumns() throws Exception {
        assertThat(School.class.getAnnotation(Table.class).name()).isEqualTo("schools");
        assertThat(Faculty.class.getAnnotation(Table.class).name()).isEqualTo("faculties");
        assertThat(Major.class.getAnnotation(Table.class).name()).isEqualTo("majors");
        assertThat(InterestCategory.class.getAnnotation(Table.class).name()).isEqualTo("interest_categories");
        assertThat(Faculty.class.getDeclaredField("school").getAnnotation(JoinColumn.class).name())
                .isEqualTo("school_id");
        assertThat(Major.class.getDeclaredField("faculty").getAnnotation(JoinColumn.class).name())
                .isEqualTo("faculty_id");
        assertThat(UserProfile.class.getDeclaredField("interests").getAnnotation(JoinTable.class).name())
                .isEqualTo("user_interests");
        assertThat(UserProfile.class.getDeclaredField("entryYear").getAnnotation(JdbcTypeCode.class).value())
                .isEqualTo(SqlTypes.SMALLINT);
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
