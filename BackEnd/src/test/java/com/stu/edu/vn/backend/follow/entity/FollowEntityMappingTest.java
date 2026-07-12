package com.stu.edu.vn.backend.follow.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.junit.jupiter.api.Test;

class FollowEntityMappingTest {

    @Test
    void followUsesCompositePrimaryKeyAndLazyUserRelations() throws Exception {
        // Mapping phải khớp bảng follows hiện có và không tạo quan hệ eager ngoài ý muốn.
        assertThat(Follow.class.getAnnotation(Table.class).name()).isEqualTo("follows");
        assertThat(Follow.class.getDeclaredField("id").getAnnotation(EmbeddedId.class)).isNotNull();
        assertLazyRelation("follower", "followerId", "follower_id");
        assertLazyRelation("following", "followingId", "following_id");
    }

    @Test
    void followIdMapsBothColumnsAndImplementsStableEquality() throws Exception {
        // Khóa kép là lớp bảo vệ cuối cùng chống một cặp Follow bị ghi trùng.
        assertThat(Serializable.class).isAssignableFrom(FollowId.class);
        assertThat(FollowId.class.getDeclaredField("followerId").getAnnotation(Column.class).name())
                .isEqualTo("follower_id");
        assertThat(FollowId.class.getDeclaredField("followingId").getAnnotation(Column.class).name())
                .isEqualTo("following_id");
        assertThat(new FollowId(1L, 2L)).isEqualTo(new FollowId(1L, 2L));
        assertThat(new FollowId(1L, 2L)).hasSameHashCodeAs(new FollowId(1L, 2L));
        assertThat(new FollowId(1L, 2L)).isNotEqualTo(new FollowId(2L, 1L));
    }

    @Test
    void followedAtIsReadOnlyBecauseMySqlCreatesIt() throws Exception {
        // created_at do MySQL sinh nên JPA chỉ đọc để trả followedAt.
        Column createdAt = Follow.class.getDeclaredField("createdAt").getAnnotation(Column.class);
        assertThat(createdAt.name()).isEqualTo("created_at");
        assertThat(createdAt.insertable()).isFalse();
        assertThat(createdAt.updatable()).isFalse();
    }

    private void assertLazyRelation(String fieldName, String mapsIdValue, String columnName) throws Exception {
        ManyToOne relation = Follow.class.getDeclaredField(fieldName).getAnnotation(ManyToOne.class);
        MapsId mapsId = Follow.class.getDeclaredField(fieldName).getAnnotation(MapsId.class);
        JoinColumn joinColumn = Follow.class.getDeclaredField(fieldName).getAnnotation(JoinColumn.class);
        assertThat(relation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(mapsId.value()).isEqualTo(mapsIdValue);
        assertThat(joinColumn.name()).isEqualTo(columnName);
    }
}
