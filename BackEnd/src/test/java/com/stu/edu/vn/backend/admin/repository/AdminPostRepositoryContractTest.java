package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

class AdminPostRepositoryContractTest {

    @Test
    void mutationQueryUsesPessimisticWriteAndDoesNotFetchCollections() throws Exception {
        Method method = AdminPostRepository.class.getMethod("findByIdForUpdate", Long.class);
        Lock lock = method.getAnnotation(Lock.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(query.value()).isEqualTo("select p from Post p where p.id = :postId");
        assertThat(query.value()).doesNotContain("media", "postHashtags", "reports", "join fetch");
    }

    @Test
    void listUsesProjectionFiltersStableOrderAndNoSensitiveColumns() throws Exception {
        Method method = AdminPostRepository.class.getMethod("findAdminPosts",
                String.class, String.class, Long.class, int.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("JOIN users a", "LEFT JOIN user_profiles ap", ":authorId IS NULL")
                .contains(":status IS NULL OR p.status = :status")
                .contains("LOWER(p.content) LIKE", "LOWER(ap.display_name) LIKE", "ESCAPE '='")
                .contains(":reportedOnly = 0 OR EXISTS", "er.status = 'PENDING'")
                .contains("ORDER BY p.created_at DESC, p.id DESC")
                .contains("ORDER BY pm.display_order ASC, pm.id ASC LIMIT 1")
                .doesNotContain("password_hash", "storage_public_id", "avatar_public_id",
                        "token_hash", "SELECT p.*", "SELECT a.*");
        assertThat(query.countQuery()).contains("COUNT(p.id)").doesNotContain("ORDER BY");
    }

    @Test
    void detailAndChildrenUseFixedSafeQueriesWithStableOrdering() throws Exception {
        Query detail = AdminPostRepository.class.getMethod("findAdminPostDetail", Long.class)
                .getAnnotation(Query.class);
        Query media = AdminPostRepository.class.getMethod("findAdminPostMedia", Long.class)
                .getAnnotation(Query.class);
        Query hashtags = AdminPostRepository.class.getMethod("findAdminPostHashtags", Long.class)
                .getAnnotation(Query.class);

        assertThat(detail.value()).contains("WHERE p.id = :postId", "p.hidden_at AS hiddenAt",
                        "p.deleted_at AS deletedAt", "a.status AS authorAccountStatus")
                .doesNotContain("p.status = 'PUBLISHED'", "a.status = 'ACTIVE'", "password_hash",
                        "avatar_public_id", "storage_public_id", "SELECT p.*");
        assertThat(media.value()).contains("ORDER BY pm.display_order ASC, pm.id ASC")
                .contains("pm.media_type AS mediaType", "pm.mime_type AS mimeType",
                        "pm.duration_seconds AS durationSeconds", "pm.thumbnail_url AS thumbnailUrl")
                .doesNotContain("storage_public_id", "file_size_bytes");
        assertThat(hashtags.value()).contains("SELECT DISTINCT h.normalized_name AS name",
                "ORDER BY h.normalized_name ASC");
    }
}
