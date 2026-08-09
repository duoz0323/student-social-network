package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import jakarta.persistence.LockModeType;

class AdminHashtagRepositoryContractTest {

    @Test
    void listUsesCounterLatestRelationSearchAndStableOrder() throws Exception {
        Method method = AdminHashtagRepository.class.getMethod(
                "findAdminHashtags", String.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("h.post_count AS postCount", "MAX(ph.created_at) AS latestUsedAt")
                .contains("LEFT JOIN post_hashtags ph ON ph.hashtag_id = h.id")
                .contains("LOWER(h.display_name) LIKE", "LOWER(h.normalized_name) LIKE", "ESCAPE '='")
                .contains("GROUP BY h.id, h.display_name, h.post_count, h.created_at")
                .contains("MAX(ph.created_at) DESC, h.id DESC")
                .doesNotContain("SELECT h.*", "SELECT ph.*");
        assertThat(query.countQuery()).contains("COUNT(h.id)").doesNotContain("JOIN post_hashtags", "ORDER BY");
    }

    @Test
    void deleteLocksHashtagAndRemovesRelationsExplicitly() throws Exception {
        Method lockMethod = AdminHashtagRepository.class.getMethod("findByIdForUpdate", Long.class);
        assertThat(lockMethod.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);

        Method deleteMethod = AdminHashtagRepository.class.getMethod("deletePostRelations", Long.class);
        Query query = deleteMethod.getAnnotation(Query.class);
        assertThat(deleteMethod.getAnnotation(Modifying.class)).isNotNull();
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value()).isEqualTo("DELETE FROM post_hashtags WHERE hashtag_id = :hashtagId");
    }
}
