package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class SavedPostRepositoryContractTest {

    @Test
    void repositoryExposesIdempotentSaveAndSingleDeleteContracts() throws Exception {
        // Repository cung cấp kiểm tra khóa kép và đúng một câu DELETE theo userId/postId cho Unsave.
        Method existsMethod = SavedPostRepository.class.getMethod(
                "existsByIdUserIdAndIdPostId",
                Long.class,
                Long.class
        );
        Method deleteMethod = SavedPostRepository.class.getMethod(
                "deleteByUserIdAndPostId",
                Long.class,
                Long.class
        );

        assertThat(existsMethod).isNotNull();
        assertThat(deleteMethod.getAnnotation(Modifying.class)).isNotNull();
        assertThat(deleteMethod.getAnnotation(Query.class).nativeQuery()).isTrue();
        assertThat(deleteMethod.getAnnotation(Query.class).value())
                .contains("DELETE FROM saved_posts")
                .contains("user_id = :userId")
                .contains("post_id = :postId");
    }
}
