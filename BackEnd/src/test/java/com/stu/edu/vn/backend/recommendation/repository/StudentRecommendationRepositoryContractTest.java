package com.stu.edu.vn.backend.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

class StudentRecommendationRepositoryContractTest {

    @Test
    void queryAppliesEligibilityScoringPrivacyAndStableDatabasePagination() throws Exception {
        Method method = StudentRecommendationRepository.class.getMethod(
                "findStudentRecommendations", Long.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("candidate_user.role = 'USER'", "candidate_user.status = 'ACTIVE'")
                .contains("candidate_profile.profile_completed_at IS NOT NULL")
                .contains("user_blocks", "blocked_pair.blocker_id = :currentUserId")
                .contains("existing_follow.follower_id = :currentUserId")
                .contains("interest.status = 'ACTIVE'", "COUNT(DISTINCT candidate_interest.interest_id)")
                .contains("candidate_school.status = 'ACTIVE'", "candidate_faculty.status = 'ACTIVE'",
                        "candidate_major.status = 'ACTIVE'")
                .contains("sameSchool * 40", "sameFaculty * 25", "sameMajor * 20", "sameEntryYear * 10")
                .contains("LEAST(commonInterestCount * 5, 25)", "LEAST(mutualConnectionCount * 3, 15)")
                .contains("WHERE matchScore > 0")
                .contains("ORDER BY matchScore DESC", "commonInterestCount DESC",
                        "mutualConnectionCount DESC", "userId ASC")
                .doesNotContain("user_restrictions", "ORDER BY RAND()", "email", "password_hash");
        assertThat(query.countQuery())
                .contains("SELECT COUNT(*)", "user_blocks", "existing_follow", "interest.status = 'ACTIVE'")
                .doesNotContain("user_restrictions");
    }
}
