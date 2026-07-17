package com.stu.edu.vn.backend.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.post.entity.Hashtag;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test tùy chọn trên MySQL thật để xác nhận thứ tự, giới hạn và tính chỉ đọc của native query autocomplete.
 */
@SpringBootTest(properties = "bootstrap-admin.enabled=false")
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
class HashtagSuggestionRepositoryIntegrationTest {

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void queryOrdersPrefixBeforeContainsLimitsTenAndDoesNotChangeData() {
        String marker = "hs" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        List<Hashtag> prefixHashtags = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            prefixHashtags.add(saveHashtag(marker + index, 100 - index));
        }
        Hashtag containsMatch = saveHashtag("x" + marker, 1000);
        Hashtag unrelated = saveHashtag("unrelated" + marker.substring(2), 2000);
        entityManager.flush();
        entityManager.clear();

        List<Hashtag> suggestions = hashtagRepository.findSuggestions(marker);

        assertThat(suggestions).hasSize(10);
        assertThat(suggestions).extracting(Hashtag::getNormalizedName)
                .containsExactlyElementsOf(prefixHashtags.subList(0, 10).stream()
                        .map(Hashtag::getNormalizedName)
                        .toList())
                .doesNotContain(containsMatch.getNormalizedName(), unrelated.getNormalizedName());
        assertThat(hashtagRepository.findByNormalizedName(prefixHashtags.getFirst().getNormalizedName())).isPresent();
        assertThat(hashtagRepository.findByNormalizedName("missing" + marker)).isEmpty();

        entityManager.clear();
        assertThat(hashtagRepository.findById(prefixHashtags.getFirst().getId()).orElseThrow().getPostCount())
                .isEqualTo(100);
        assertThat(hashtagRepository.count()).isGreaterThanOrEqualTo(14);
    }

    private Hashtag saveHashtag(String name, int postCount) {
        Hashtag hashtag = new Hashtag(name, name);
        hashtag.setPostCount(postCount);
        return hashtagRepository.saveAndFlush(hashtag);
    }
}
