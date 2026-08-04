package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

class SearchPostRepositoryContractTest {

    @Test
    void cursorQueriesContainApprovedFiltersAndStableOrdering() throws Exception {
        Query content = query(
                PostRepository.class, "searchPublishedPostsByContentAfter",
                String.class, Long.class, Double.class, LocalDateTime.class, Long.class, Pageable.class);
        assertThat(content.value()).contains("MATCH(p.content)", ":keyword", "p.status = 'PUBLISHED'",
                "u.status = 'ACTIVE'", "up.profile_completed_at IS NOT NULL", "user_blocks", ":viewerId",
                ":cursorRelevance", ":cursorTime", ":cursorPostId", "p.published_at DESC", "p.id DESC");

        Query hashtag = query(
                PostRepository.class, "searchPublishedPostsByHashtagAfter",
                String.class, Long.class, LocalDateTime.class, Long.class, Pageable.class);
        assertThat(hashtag.value()).contains("h.normalized_name = :normalizedName", "p.status = 'PUBLISHED'",
                "u.status = 'ACTIVE'", "up.profile_completed_at IS NOT NULL", "user_blocks", ":viewerId",
                ":cursorTime", ":cursorPostId", "p.published_at DESC", "p.id DESC");
    }

    @Test
    void enrichmentRepositoriesExposeBatchInQueries() throws Exception {
        assertThat(PostMediaRepository.class.getMethod(
                "findByPost_IdInOrderByPost_IdAscDisplayOrderAsc", Collection.class)).isNotNull();
        assertThat(query(PostHashtagRepository.class, "findWithHashtagByPostIds", Collection.class).value())
                .contains("in :postIds", "join fetch ph.hashtag");
        assertThat(query(PostLikeRepository.class, "findLikedPostIds", Long.class, Collection.class).value())
                .contains("pl.id.userId = :userId", "in :postIds");
        assertThat(query(SavedPostRepository.class, "findSavedPostIds", Long.class, Collection.class).value())
                .contains("sp.id.userId = :userId", "in :postIds");
    }

    private Query query(Class<?> type, String name, Class<?>... parameters) throws Exception {
        Method method = type.getMethod(name, parameters);
        return method.getAnnotation(Query.class);
    }
}
