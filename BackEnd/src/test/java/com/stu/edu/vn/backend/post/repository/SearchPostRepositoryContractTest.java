package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

class SearchPostRepositoryContractTest {

    @Test
    void pageableQueriesContainApprovedFiltersBindingCountAndOrdering() throws Exception {
        Query content = query(PostRepository.class, "searchPublishedPostsByContent", String.class, Pageable.class);
        assertThat(content.value()).contains("MATCH(p.content)", ":keyword", "p.status = 'PUBLISHED'",
                "u.status = 'ACTIVE'", "up.profile_completed_at IS NOT NULL", "p.published_at DESC", "p.id DESC");
        assertThat(content.countQuery()).contains("COUNT(*)", "MATCH(p.content)", ":keyword");

        Query hashtag = query(PostRepository.class, "searchPublishedPostsByHashtag", String.class, Pageable.class);
        assertThat(hashtag.value()).contains("h.normalized_name = :normalizedName", "p.status = 'PUBLISHED'",
                "u.status = 'ACTIVE'", "up.profile_completed_at IS NOT NULL", "p.published_at DESC", "p.id DESC");
        assertThat(hashtag.countQuery()).contains("COUNT(*)", "h.normalized_name = :normalizedName");
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
