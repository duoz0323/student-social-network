package com.stu.edu.vn.backend.discovery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.Test;

/** Khóa các invariant SQL để tránh vô tình thêm Restrict/Repost/OFFSET/COUNT. */
class DiscoveryMapRepositoryContractTest {

    @Test
    void markerQueryAggregatesOnlyVisibleOriginalPostsWithDeterministicLimit() {
        String sql = DiscoveryMapRepository.FIND_MAP_LOCATIONS_SQL.toLowerCase(Locale.ROOT);

        assertThat(sql)
                .contains("count(p.id) as post_count")
                .contains("max(p.published_at) as latest_post_at")
                .contains("l.latitude between :south and :north")
                .contains("l.longitude between :west and :east")
                .contains("p.status = 'published'")
                .contains("author.role = 'user'")
                .contains("author.status = 'active'")
                .contains("author_profile.profile_completed_at is not null")
                .contains("viewer_block.blocker_id = :viewerid")
                .contains("author_block.blocked_id = :viewerid")
                .contains("group by l.id")
                .contains("order by latest_post_at desc, l.id desc")
                .contains("limit :resultlimit")
                .doesNotContain("user_restrictions")
                .doesNotContain("post_reposts")
                .doesNotContain(" offset ");
    }

    @Test
    void locationPostQueryUsesCompleteKeysetAndNoCountOrOffset() {
        String sql = DiscoveryMapRepository.FIND_LOCATION_POST_KEYS_SQL.toLowerCase(Locale.ROOT);

        assertThat(sql)
                .contains("p.location_id = :locationid")
                .contains("p.status = 'published'")
                .contains("author.role = 'user'")
                .contains("author.status = 'active'")
                .contains("author_profile.profile_completed_at is not null")
                .contains("p.published_at < :cursorpublishedat")
                .contains("p.id < :cursorpostid")
                .contains("order by p.published_at desc, p.id desc")
                .contains("limit :resultlimit")
                .doesNotContain("count(")
                .doesNotContain(" offset ")
                .doesNotContain("user_restrictions")
                .doesNotContain("post_reposts");
    }
}
