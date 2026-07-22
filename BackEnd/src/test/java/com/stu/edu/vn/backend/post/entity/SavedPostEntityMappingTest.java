package com.stu.edu.vn.backend.post.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.junit.jupiter.api.Test;

class SavedPostEntityMappingTest {

    @Test
    void savedPostMappingMatchesExistingTable() throws Exception {
        // Xác nhận Entity dùng đúng bảng, khóa kép và hai quan hệ lazy theo schema saved_posts hiện có.
        assertThat(SavedPost.class.getAnnotation(Table.class).name()).isEqualTo("saved_posts");
        assertThat(SavedPost.class.getDeclaredField("id").getAnnotation(EmbeddedId.class)).isNotNull();

        ManyToOne userRelation = SavedPost.class.getDeclaredField("user").getAnnotation(ManyToOne.class);
        ManyToOne postRelation = SavedPost.class.getDeclaredField("post").getAnnotation(ManyToOne.class);
        assertThat(userRelation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(postRelation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(SavedPost.class.getDeclaredField("user").getAnnotation(JoinColumn.class).name())
                .isEqualTo("user_id");
        assertThat(SavedPost.class.getDeclaredField("post").getAnnotation(JoinColumn.class).name())
                .isEqualTo("post_id");

        Column createdAt = SavedPost.class.getDeclaredField("createdAt").getAnnotation(Column.class);
        assertThat(createdAt.name()).isEqualTo("created_at");
        assertThat(createdAt.insertable()).isFalse();
        assertThat(createdAt.updatable()).isFalse();
    }

    @Test
    void savedPostIdUsesUserAndPostAsCompositePrimaryKey() {
        // Khóa kép không có id riêng và triển khai equals/hashCode để JPA nhận diện đúng một quan hệ Save.
        assertThat(Serializable.class).isAssignableFrom(SavedPostId.class);
        assertThat(new SavedPostId(10L, 15L)).isEqualTo(new SavedPostId(10L, 15L));
        assertThat(new SavedPostId(10L, 15L)).hasSameHashCodeAs(new SavedPostId(10L, 15L));
        assertThat(new SavedPostId(10L, 15L)).isNotEqualTo(new SavedPostId(11L, 15L));
    }
}
