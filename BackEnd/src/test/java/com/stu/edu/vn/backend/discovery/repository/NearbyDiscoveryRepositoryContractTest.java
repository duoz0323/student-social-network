package com.stu.edu.vn.backend.discovery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class NearbyDiscoveryRepositoryContractTest {

    @Test
    void queryKeepsAllCandidateDistanceBlockOrderingAndKeysetRulesInsideMySql() {
        String sql = NearbyDiscoveryRepository.FIND_NEARBY_SQL.toLowerCase(Locale.ROOT);

        assertThat(sql)
                .contains("p.status = 'published'")
                .contains("p.location_id is not null")
                .contains("author.role = 'user'")
                .contains("author.status = 'active'")
                .contains("author_profile.profile_completed_at is not null")
                .contains("not exists")
                .contains("blocked_relation.blocker_id = :viewerid")
                .contains("blocked_relation.blocked_id = :viewerid")
                .contains("exact_distance_meters <= :radiusmeters")
                .contains("cast(round(exact_distance_meters, 0) as signed)")
                .contains("distance_meters > :cursordistancemeters")
                .contains("published_at < :cursorpublishedat")
                .contains("post_id < :cursorpostid")
                .contains("order by distance_meters asc, published_at desc, post_id desc")
                .contains("limit :resultlimit")
                .doesNotContain("count(")
                .doesNotContain(" offset ")
                .doesNotContain("user_restrictions")
                .doesNotContain("post_reposts");
    }
}
