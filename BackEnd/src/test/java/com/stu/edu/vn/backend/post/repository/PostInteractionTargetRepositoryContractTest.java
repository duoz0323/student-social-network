package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

class PostInteractionTargetRepositoryContractTest {

    @Test
    void interactionTargetQueryReturnsStatusAndAuthorInOneProjection() throws Exception {
        Method method = PostRepository.class.getMethod("findInteractionTargetById", Long.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isFalse();
        assertThat(query.value())
                .contains("post.id AS postId")
                .contains("post.author.id AS authorId")
                .contains("post.status AS status")
                .doesNotContain("JOIN FETCH", "SELECT post FROM");
    }
}
