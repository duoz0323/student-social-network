package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class PostRepositoryContractTest {

    @Test
    void postRepositoryExposesRequiredLookupMethods() throws Exception {
        // Repository có đủ method cho detail, kiểm tra tác giả và truy vấn theo trạng thái.
        Method findByIdAndStatus = PostRepository.class.getMethod("findByIdAndStatus", Long.class, PostStatus.class);
        Method existsByAuthor = PostRepository.class.getMethod("existsByIdAndAuthor_Id", Long.class, Long.class);
        Method detailMethod = PostRepository.class.getMethod("findDetailHeaderByIdAndStatus", Long.class, PostStatus.class);
        Method softDeleteMethod = PostRepository.class.getMethod("softDeletePublishedPost", Long.class, LocalDateTime.class);

        assertThat(findByIdAndStatus).isNotNull();
        assertThat(existsByAuthor).isNotNull();
        assertThat(softDeleteMethod.getAnnotation(Modifying.class)).isNotNull();
        assertThat(detailMethod.getAnnotation(EntityGraph.class).attributePaths())
                .containsExactlyInAnyOrder("author", "authorProfile", "location");
    }

    @Test
    void mediaAndHashtagRepositoriesExposeRequiredMethods() throws Exception {
        // Repository phụ hỗ trợ lấy media đúng thứ tự và lấy/xóa quan hệ hashtag của một bài.
        assertThat(PostMediaRepository.class.getMethod("findByPost_IdOrderByDisplayOrderAsc", Long.class)).isNotNull();
        assertThat(HashtagRepository.class.getMethod("findByNormalizedName", String.class)).isNotNull();
        assertThat(PostHashtagRepository.class.getMethod("findByPost_Id", Long.class)).isNotNull();
        assertThat(PostHashtagRepository.class.getMethod("deleteByPostId", Long.class).getAnnotation(Modifying.class))
                .isNotNull();
        assertThat(PostHashtagRepository.class.getMethod("findWithHashtagByPostId", Long.class)
                .getAnnotation(EntityGraph.class).attributePaths()).containsExactly("hashtag");
    }

    @Test
    void cursorQueriesUseKeysetWithoutCountQuery() throws Exception {
        // Các danh sách cuộn vô hạn phải giới hạn bằng Pageable trang 0 nhưng không được sinh COUNT/OFFSET nghiệp vụ.
        Method forYou = PostRepository.class.getMethod(
                "findForYouFeed", Long.class, int.class, LocalDateTime.class, Long.class,
                org.springframework.data.domain.Pageable.class);
        Method following = PostRepository.class.getMethod(
                "findFollowingFeed", Long.class, LocalDateTime.class, Long.class,
                org.springframework.data.domain.Pageable.class);
        Method profile = PostRepository.class.getMethod(
                "findProfilePosts", Long.class, Long.class, LocalDateTime.class, Long.class,
                org.springframework.data.domain.Pageable.class);
        Method saved = PostRepository.class.getMethod(
                "findSavedPosts", Long.class, LocalDateTime.class, Long.class,
                org.springframework.data.domain.Pageable.class);
        Method liked = PostRepository.class.getMethod(
                "findLikedPosts", Long.class, LocalDateTime.class, Long.class,
                org.springframework.data.domain.Pageable.class);

        for (Method method : new Method[]{forYou, following, profile, saved, liked}) {
            Query query = method.getAnnotation(Query.class);
            assertThat(method.getReturnType()).isEqualTo(java.util.List.class);
            assertThat(query.countQuery()).isBlank();
            assertThat(query.value()).doesNotContainIgnoringCase("OFFSET", "COUNT(");
            assertThat(query.value()).contains(
                    "p.status = 'PUBLISHED'",
                    "p.id < :cursorPostId",
                    "user_blocks",
                    ":viewerId"
            );
        }
        assertThat(forYou.getAnnotation(Query.class).value())
                .contains("(p.like_count + p.comment_count)", "ORDER BY", "p.id DESC", "user_blocks");
        assertThat(following.getAnnotation(Query.class).value())
                .contains("f.follower_id = :viewerId", "f.following_id = p.author_id", "user_blocks");
    }
}
