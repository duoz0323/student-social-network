package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

class AdminActionRepositoryContractTest {

    @Test
    void listUsesDatabasePaginationAllFiltersAndStableDescendingOrder() throws Exception {
        Method method = AdminActionRepository.class.getMethod("findAdminActions",
                String.class, String.class, Long.class, LocalDateTime.class, LocalDateTime.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("FROM admin_actions aa", "LEFT JOIN user_profiles ap")
                .contains(":actionType IS NULL", ":targetType IS NULL", ":adminId IS NULL")
                .contains("aa.created_at >= :fromTime", "aa.created_at <= :toTime")
                .contains("ORDER BY aa.created_at DESC, aa.id DESC")
                .doesNotContain("old_data", "new_data", "password_hash", "token_hash", "SELECT aa.*");
        assertThat(query.countQuery()).contains("COUNT(aa.id)").doesNotContain("ORDER BY");
    }

    @Test
    void detailReadsOnlyApprovedJsonAndBatchQueriesUseBoundCollections() throws Exception {
        Query detail = AdminActionRepository.class.getMethod("findAdminActionDetail", Long.class)
                .getAnnotation(Query.class);
        Query users = AdminActionRepository.class.getMethod("findUserTargets", Collection.class)
                .getAnnotation(Query.class);
        Query posts = AdminActionRepository.class.getMethod("findPostTargets", Collection.class)
                .getAnnotation(Query.class);
        Query reports = AdminActionRepository.class.getMethod("findReportTargets", Collection.class)
                .getAnnotation(Query.class);

        assertThat(detail.value()).contains("WHERE aa.id = :actionId", "CAST(aa.old_data AS CHAR)",
                        "CAST(aa.new_data AS CHAR)")
                .doesNotContain("password_hash", "token_hash", "SELECT aa.*", "SELECT admin_user.*");
        assertThat(users.value()).contains("u.id IN (:targetIds)", "up.display_name AS displayName");
        assertThat(posts.value()).contains("p.id IN (:targetIds)");
        assertThat(reports.value()).contains("r.id IN (:targetIds)");
    }
}
