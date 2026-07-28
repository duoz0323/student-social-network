package com.stu.edu.vn.backend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class UserBlockRepositoryContractTest {

    @Test
    void atomicWriteAndDeleteKeepIdempotentContract() throws Exception {
        Method insert = UserBlockRepository.class.getMethod(
                "insertIfAbsent", Long.class, Long.class);
        Method delete = UserBlockRepository.class.getMethod(
                "deleteBlock", Long.class, Long.class);

        assertThat(insert.getAnnotation(Modifying.class)).isNotNull();
        assertThat(insert.getAnnotation(Query.class).value())
                .containsIgnoringCase("INSERT INTO")
                .containsIgnoringCase("ON DUPLICATE KEY UPDATE")
                .contains("blocker_id", "blocked_id")
                .doesNotContainIgnoringCase("INSERT IGNORE");
        assertThat(delete.getAnnotation(Modifying.class)).isNotNull();
    }

    @Test
    void listProjectionUsesCurrentBlockerAndNeverSelectsSensitiveFields() throws Exception {
        Method list = UserBlockRepository.class.getMethod(
                "findBlockedUsers", Long.class, Pageable.class);
        Query query = list.getAnnotation(Query.class);

        assertThat(query.value())
                .contains("ub.blocker_id = :blockerId", "up.display_name", "up.avatar_url")
                .doesNotContain("email", "password_hash", "refresh_token");
    }
}
