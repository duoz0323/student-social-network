package com.stu.edu.vn.backend.post.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import org.junit.jupiter.api.Test;

class PostEntityMappingTest {

    @Test
    void postMappingMatchesPostsTable() throws Exception {
        // Test mapping chính của bảng posts để tránh lệch tên bảng, enum và quan hệ tác giả.
        assertThat(BaseAuditEntity.class).isAssignableFrom(Post.class);
        assertThat(Post.class.getAnnotation(Table.class).name()).isEqualTo("posts");
        assertThat(Post.class.getDeclaredField("status").getAnnotation(Enumerated.class).value())
                .isEqualTo(EnumType.STRING);
        assertThat(Post.class.getDeclaredField("content").getAnnotation(Column.class).length()).isEqualTo(500);
        assertThat(Post.class.getDeclaredField("author").getAnnotation(ManyToOne.class).fetch())
                .isEqualTo(FetchType.LAZY);
        assertThat(Post.class.getDeclaredField("author").getAnnotation(JoinColumn.class).name())
                .isEqualTo("author_id");
    }

    @Test
    void postCollectionsDoNotCascadeRemoveToSharedHashtagData() throws Exception {
        // Post chỉ cascade persist/merge cho bản ghi phụ, không cascade remove sang Hashtag dùng chung.
        OneToMany mediaRelation = Post.class.getDeclaredField("media").getAnnotation(OneToMany.class);
        OneToMany hashtagRelation = Post.class.getDeclaredField("postHashtags").getAnnotation(OneToMany.class);

        assertThat(mediaRelation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(hashtagRelation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(mediaRelation.cascade()).containsExactlyInAnyOrder(CascadeType.PERSIST, CascadeType.MERGE);
        assertThat(hashtagRelation.cascade()).containsExactlyInAnyOrder(CascadeType.PERSIST, CascadeType.MERGE);
        assertThat(hashtagRelation.cascade()).doesNotContain(CascadeType.REMOVE);
    }

    @Test
    void postLocationIsOptionalLazyManyToOneWithoutRemoveCascade() throws Exception {
        // Location là quan hệ dùng chung và tùy chọn nên Post chỉ giữ khóa ngoại nullable, không sở hữu vòng đời Location.
        java.lang.reflect.Field locationField = Post.class.getDeclaredField("location");
        ManyToOne relation = locationField.getAnnotation(ManyToOne.class);
        JoinColumn joinColumn = locationField.getAnnotation(JoinColumn.class);

        assertThat(relation).isNotNull();
        assertThat(relation.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(relation.optional()).isTrue();
        assertThat(relation.cascade()).doesNotContain(CascadeType.ALL, CascadeType.REMOVE);
        assertThat(joinColumn.name()).isEqualTo("location_id");
        assertThat(joinColumn.nullable()).isTrue();
        ForeignKey foreignKey = joinColumn.foreignKey();
        assertThat(foreignKey.name()).isEqualTo("fk_posts_location");
        assertThat(locationField.getAnnotation(OneToOne.class)).isNull();
        assertThat(locationField.getAnnotation(OneToMany.class)).isNull();
    }

    @Test
    void postMediaMappingMatchesPostMediaTable() throws Exception {
        // Test metadata ảnh bài viết khớp schema và quan hệ về Post luôn lazy.
        assertThat(PostMedia.class.getAnnotation(Table.class).name()).isEqualTo("post_media");
        assertThat(PostMedia.class.getDeclaredField("post").getAnnotation(ManyToOne.class).fetch())
                .isEqualTo(FetchType.LAZY);
        assertThat(PostMedia.class.getDeclaredField("mediaUrl").getAnnotation(Column.class).length()).isEqualTo(1000);
        assertThat(PostMedia.class.getDeclaredField("storagePublicId").getAnnotation(Column.class).unique()).isTrue();
        assertThat(PostMedia.class.getDeclaredField("displayOrder").getAnnotation(Column.class).name())
                .isEqualTo("display_order");
    }

    @Test
    void hashtagMappingMatchesHashtagsTable() throws Exception {
        // Hashtag kế thừa audit và dùng normalized_name unique để chống trùng sau chuẩn hóa.
        assertThat(BaseAuditEntity.class).isAssignableFrom(Hashtag.class);
        assertThat(Hashtag.class.getAnnotation(Table.class).name()).isEqualTo("hashtags");
        Column normalizedName = Hashtag.class.getDeclaredField("normalizedName").getAnnotation(Column.class);
        assertThat(normalizedName.name()).isEqualTo("normalized_name");
        assertThat(normalizedName.length()).isEqualTo(100);
        assertThat(normalizedName.unique()).isTrue();
    }

    @Test
    void postHashtagUsesCompositePrimaryKey() throws Exception {
        // Bảng post_hashtags dùng khóa kép post_id và hashtag_id thay vì id riêng.
        assertThat(Serializable.class).isAssignableFrom(PostHashtagId.class);
        assertThat(PostHashtag.class.getAnnotation(Table.class).name()).isEqualTo("post_hashtags");
        assertThat(PostHashtag.class.getAnnotation(Table.class).uniqueConstraints())
                .extracting(UniqueConstraint::name)
                .containsExactly("uq_post_hashtags_post");
        assertThat(PostHashtag.class.getAnnotation(Table.class).uniqueConstraints()[0].columnNames())
                .containsExactly("post_id");
        assertThat(PostHashtag.class.getDeclaredField("id").getAnnotation(EmbeddedId.class)).isNotNull();
        assertThat(new PostHashtagId(1L, 2L)).isEqualTo(new PostHashtagId(1L, 2L));
        assertThat(new PostHashtagId(1L, 2L)).hasSameHashCodeAs(new PostHashtagId(1L, 2L));
        assertThat(new PostHashtagId(1L, 2L)).isNotEqualTo(new PostHashtagId(1L, 3L));
    }
}
