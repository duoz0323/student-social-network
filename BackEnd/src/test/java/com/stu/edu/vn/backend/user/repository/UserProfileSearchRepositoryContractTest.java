package com.stu.edu.vn.backend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

class UserProfileSearchRepositoryContractTest {

    @Test
    void searchQueryUsesBoundSubstringFilteringApprovedFiltersAndStableOrdering() throws Exception {
        Method method = UserProfileRepository.class.getMethod(
                "searchCompletedActiveProfilesByDisplayName", String.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("LIKE CONCAT('%', :keyword, '%')")
                .contains("u.status = 'ACTIVE'")
                .contains("up.profile_completed_at IS NOT NULL")
                .contains("up.display_name IS NOT NULL")
                .contains("CASE WHEN up.display_name LIKE CONCAT(:keyword, '%')")
                .contains("up.user_id DESC")
                .doesNotContain("email", "phone_number", "password_hash");
        assertThat(query.countQuery())
                .contains("COUNT(*)", "LIKE CONCAT('%', :keyword, '%')", "u.status = 'ACTIVE'");
    }
}
