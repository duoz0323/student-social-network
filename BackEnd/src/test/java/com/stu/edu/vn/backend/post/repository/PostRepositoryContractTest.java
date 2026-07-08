package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;

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
                .containsExactlyInAnyOrder("author", "authorProfile");
    }

    @Test
    void mediaAndHashtagRepositoriesExposeRequiredMethods() throws Exception {
        // Repository phụ hỗ trợ lấy media đúng thứ tự và lấy/xóa quan hệ hashtag của một bài.
        assertThat(PostMediaRepository.class.getMethod("findByPost_IdOrderByDisplayOrderAsc", Long.class)).isNotNull();
        assertThat(HashtagRepository.class.getMethod("findByNormalizedName", String.class)).isNotNull();
        assertThat(HashtagRepository.class.getMethod("findByNormalizedNameIn", java.util.Collection.class)).isNotNull();
        assertThat(PostHashtagRepository.class.getMethod("findByPost_Id", Long.class)).isNotNull();
        assertThat(PostHashtagRepository.class.getMethod("deleteByPostId", Long.class).getAnnotation(Modifying.class))
                .isNotNull();
        assertThat(PostHashtagRepository.class.getMethod("findWithHashtagByPostId", Long.class)
                .getAnnotation(EntityGraph.class).attributePaths()).containsExactly("hashtag");
    }
}
