package com.stu.edu.vn.backend.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

class AdminUserRepositoryContractTest {

    @Test
    void statusMutationQueryUsesPessimisticWriteLock() throws Exception {
        Method method = AdminUserRepository.class.getMethod("findByIdForUpdate", Long.class);
        Lock lock = method.getAnnotation(Lock.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(query.value()).contains("from User u", "u.id = :userId");
    }

    @Test
    void listQuerySearchesCanonicalFieldsExcludesAdminAndUsesStableOrdering() throws Exception {
        Method method = AdminUserRepository.class.getMethod(
                "findManagedUsers", String.class, String.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("LEFT JOIN user_profiles up ON up.user_id = u.id")
                .contains("u.role = 'USER'")
                .contains(":status IS NULL OR u.status = :status")
                .contains("LOWER(u.email) LIKE")
                .contains("LOWER(up.display_name) LIKE")
                .contains("CAST(NULL AS CHAR) AS phoneNumber")
                .contains("ESCAPE '='")
                .contains("ORDER BY u.created_at DESC, u.id DESC")
                .doesNotContain("phone_number", "password_hash", "avatar_public_id", "SELECT u.*", "SELECT up.*");
        assertThat(query.countQuery())
                .contains("COUNT(u.id)", "u.role = 'USER'", "LEFT JOIN user_profiles")
                .doesNotContain("ORDER BY");
    }

    @Test
    void detailQueryUsesOneJoinAndSelectsNoSensitiveColumns() throws Exception {
        Method method = AdminUserRepository.class.getMethod("findManagedUserDetail", Long.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("LEFT JOIN user_profiles up ON up.user_id = u.id")
                .contains("u.role AS role")
                .contains("WHERE u.id = :userId")
                .doesNotContain("password_hash", "avatar_public_id", "refresh_tokens", "SELECT u.*", "SELECT up.*");
        assertThat(countOccurrences(query.value(), "JOIN")).isEqualTo(1);
    }

    private int countOccurrences(String source, String value) {
        return (source.length() - source.replace(value, "").length()) / value.length();
    }
}
