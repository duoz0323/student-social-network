package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class HashtagSuggestionRepositoryContractTest {

    @Test
    void suggestionQueryFiltersContainsAndOrdersPrefixPopularityThenStableId() throws Exception {
        Method method = HashtagRepository.class.getMethod("findSuggestions", String.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("h.normalized_name LIKE CONCAT('%', :keyword, '%') ESCAPE '='")
                .contains("h.normalized_name LIKE CONCAT(:keyword, '%') ESCAPE '=' THEN 0")
                .contains("h.post_count DESC")
                .contains("h.id DESC")
                .contains("LIMIT 10");
    }

    @Test
    void suggestionQueryIsReadOnlyAndLimitIsNotControlledByClient() throws Exception {
        Method method = HashtagRepository.class.getMethod("findSuggestions", String.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getAnnotation(Modifying.class)).isNull();
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(query.value().stripLeading()).startsWith("SELECT h.*");
        assertThat(query.value()).doesNotContain("INSERT", "UPDATE", "DELETE");
    }
}
