package com.stu.edu.vn.backend.follow.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class FollowRepositoryContractTest {

    @Test
    void deleteUsesExplicitJpqlCompositeKeyAndReturnsAffectedRows() throws Exception {
        // DELETE phải nêu rõ hai trường của EmbeddedId và trả int để xử lý Unfollow nguyên tử.
        Method method = FollowRepository.class.getMethod("deleteFollow", Long.class, Long.class);
        Query query = method.getAnnotation(Query.class);
        assertThat(method.getReturnType()).isEqualTo(int.class);
        assertThat(method.getAnnotation(Modifying.class)).isNotNull();
        assertThat(query.nativeQuery()).isFalse();
        assertThat(query.value()).contains("f.id.followerId", "f.id.followingId");
    }

    @Test
    void listQueriesReturnFollowedAtAndCalculateFollowStateInSameQuery() throws Exception {
        // Hai query phải lấy created_at và dùng EXISTS, không để Service gọi exists trong vòng lặp.
        Query followers = queryOf("findActiveFollowers");
        Query following = queryOf("findActiveFollowing");

        assertListQueryContract(followers);
        assertListQueryContract(following);
        assertThat(followers.value()).contains(
                "follower.status = com.stu.edu.vn.backend.user.enums.UserStatus.ACTIVE",
                "relation.id.followerId DESC"
        );
        assertThat(following.value()).contains(
                "following_user.status = com.stu.edu.vn.backend.user.enums.UserStatus.ACTIVE",
                "relation.id.followingId DESC"
        );
    }

    private Query queryOf(String methodName) throws Exception {
        return FollowRepository.class.getMethod(methodName, Long.class, Long.class).getAnnotation(Query.class);
    }

    private void assertListQueryContract(Query query) {
        // JPQL giữ kiểu Boolean qua Hibernate, tránh native CASE 0/1 bị chiếu thành Long trên MySQL.
        assertThat(query.nativeQuery()).isFalse();
        assertThat(query.value())
                .contains("relation.createdAt AS followedAt")
                .containsIgnoringCase("CASE WHEN EXISTS")
                .contains("THEN TRUE ELSE FALSE")
                .contains("AS followedByCurrentUser")
                .contains("FROM UserBlock blockRelation")
                .contains("blockRelation.id.blockerId = :currentUserId")
                .contains("blockRelation.id.blockedId = :currentUserId")
                .contains("ORDER BY relation.createdAt DESC")
                .doesNotContain("SELECT *", "LIMIT");
    }
}
