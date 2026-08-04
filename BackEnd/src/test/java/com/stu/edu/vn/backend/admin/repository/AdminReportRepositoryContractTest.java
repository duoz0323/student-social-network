package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

class AdminReportRepositoryContractTest {

    @Test
    void mutationQueryLocksOnlyReportWithPessimisticWrite() throws Exception {
        Method method = AdminReportRepository.class.getMethod("findByIdForUpdate", Long.class);
        Lock lock = method.getAnnotation(Lock.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(query.value()).isEqualTo(
                "select r from Report r where r.id = :reportId and r.moderationCase is null");
        assertThat(query.value()).doesNotContain("join fetch", "post", "media", "snapshot");
    }

    @Test
    void listUsesOneProjectionAndCountQueryWithAllFiltersAndStableOrders() throws Exception {
        Method method = AdminReportRepository.class.getMethod("findAdminReports",
                String.class, String.class, Long.class, Long.class, Long.class, String.class,
                int.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("FROM reports r", "JOIN users reporter", "JOIN posts p", "JOIN users author")
                .contains(":status IS NULL", ":reason IS NULL", ":postId IS NULL",
                        ":reporterId IS NULL", ":authorId IS NULL")
                .contains("LOWER(r.post_content_snapshot) LIKE", "LOWER(rp.display_name) LIKE",
                        "LOWER(ap.display_name) LIKE", "ESCAPE '='")
                .contains("JSON_LENGTH(r.post_media_snapshot)")
                .contains("CASE WHEN :pendingOrder = 1 THEN r.created_at END ASC",
                        "CASE WHEN :pendingOrder = 0 THEN r.created_at END DESC")
                .doesNotContain("password_hash", "avatar_public_id", "token_hash", "post_media pm",
                        "SELECT r.*", "SELECT p.*");
        assertThat(query.countQuery()).contains("COUNT(r.id)").doesNotContain("ORDER BY");
    }

    @Test
    void detailUsesSingleSafeJoinAndReadsSnapshotInsteadOfCurrentPostMedia() throws Exception {
        Query query = AdminReportRepository.class.getMethod("findAdminReportDetail", Long.class)
                .getAnnotation(Query.class);

        assertThat(query.value())
                .contains("WHERE r.id = :reportId", "r.post_content_snapshot AS contentSnapshot",
                        "CAST(r.post_media_snapshot AS CHAR) AS mediaSnapshot",
                        "LEFT JOIN users resolver", "p.hidden_at AS hiddenAt", "p.deleted_at AS deletedAt")
                .doesNotContain("p.status = 'PUBLISHED'", "reporter.status = 'ACTIVE'",
                        "author.status = 'ACTIVE'", "JOIN post_media", "password_hash", "avatar_public_id",
                        "token_hash", "SELECT r.*", "SELECT p.*");
    }
}
