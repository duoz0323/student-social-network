package com.stu.edu.vn.backend.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.mapper.HashtagMapper;
import com.stu.edu.vn.backend.post.repository.HashtagRepository;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

class HashtagServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final HashtagRepository hashtagRepository = org.mockito.Mockito.mock(HashtagRepository.class);
    private HashtagServiceImpl hashtagService;

    @BeforeEach
    void setUp() {
        hashtagService = new HashtagServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                hashtagRepository,
                new HashtagNormalizer(),
                Mappers.getMapper(HashtagMapper.class)
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile(10L, true)));
    }

    @Test
    void normalizesLeadingHashLowercaseAndUnicodeNfcBeforeQuery() {
        when(hashtagRepository.findSuggestions("doan")).thenReturn(List.of());
        when(hashtagRepository.findByNormalizedName("doan")).thenReturn(Optional.empty());

        var response = hashtagService.getSuggestions("  ##Doan  ");

        assertThat(response.keyword()).isEqualTo("  ##Doan  ");
        assertThat(response.normalizedKeyword()).isEqualTo("doan");
        verify(hashtagRepository).findSuggestions("doan");

        String decomposedKeyword = "#A\u0301n";
        when(hashtagRepository.findSuggestions("án")).thenReturn(List.of());
        when(hashtagRepository.findByNormalizedName("án")).thenReturn(Optional.empty());
        assertThat(hashtagService.getSuggestions(decomposedKeyword).normalizedKeyword()).isEqualTo("án");
    }

    @Test
    void blankAndOneCharacterKeywordsReturnEmptyWithoutHashtagQueries() {
        var emptyResponse = hashtagService.getSuggestions("  \u00A0  ");
        var shortResponse = hashtagService.getSuggestions("#a");

        assertThat(emptyResponse.suggestions()).isEmpty();
        assertThat(emptyResponse.normalizedKeyword()).isEmpty();
        assertThat(shortResponse.suggestions()).isEmpty();
        assertThat(shortResponse.normalizedKeyword()).isEqualTo("a");
        assertThat(emptyResponse.exactMatch()).isFalse();
        assertThat(emptyResponse.canUseAsNewHashtag()).isTrue();
        verify(hashtagRepository, never()).findSuggestions(anyString());
        verify(hashtagRepository, never()).findByNormalizedName(anyString());
    }

    @Test
    void rejectsKeywordLongerThanOneHundredCharactersWithoutHashtagQueries() {
        assertThatThrownBy(() -> hashtagService.getSuggestions("a".repeat(101)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HASHTAG_SUGGESTION_KEYWORD_TOO_LONG);

        verify(hashtagRepository, never()).findSuggestions(anyString());
        verify(hashtagRepository, never()).findByNormalizedName(anyString());
    }

    @Test
    void mapsOnlyApprovedSuggestionFields() {
        Hashtag hashtag = hashtag(21L, "doantruong", "DoanTruong", 100);
        when(hashtagRepository.findSuggestions("doan")).thenReturn(List.of(hashtag));
        when(hashtagRepository.findByNormalizedName("doan")).thenReturn(Optional.empty());

        var response = hashtagService.getSuggestions("Doan");

        assertThat(response.suggestions()).singleElement()
                .extracting("hashtagId", "name", "postCount")
                .containsExactly(21L, "DoanTruong", 100);
    }

    @Test
    void exactMatchDisablesUsingKeywordAsNewHashtag() {
        Hashtag exact = hashtag(22L, "doan", "doan", 5);
        when(hashtagRepository.findSuggestions("doan")).thenReturn(List.of(exact));
        when(hashtagRepository.findByNormalizedName("doan")).thenReturn(Optional.of(exact));

        var response = hashtagService.getSuggestions("doan");

        assertThat(response.exactMatch()).isTrue();
        assertThat(response.canUseAsNewHashtag()).isFalse();
    }

    @Test
    void missingExactMatchAllowsUsingKeywordAsNewHashtag() {
        when(hashtagRepository.findSuggestions("doan")).thenReturn(List.of());
        when(hashtagRepository.findByNormalizedName("doan")).thenReturn(Optional.empty());

        var response = hashtagService.getSuggestions("doan");

        assertThat(response.exactMatch()).isFalse();
        assertThat(response.canUseAsNewHashtag()).isTrue();
    }

    @Test
    void rejectsMissingBlockedAndIncompleteCurrentUserBeforeHashtagQueries() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(ErrorCode.USER_NOT_FOUND);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.BLOCKED)));
        assertError(ErrorCode.USER_BLOCKED);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(ErrorCode.PROFILE_NOT_FOUND);

        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile(10L, false)));
        assertError(ErrorCode.PROFILE_NOT_COMPLETED);
        verify(hashtagRepository, never()).findSuggestions(anyString());
    }

    @Test
    void rejectsNonBlankKeywordThatIsInvalidAfterSharedNormalization() {
        assertThatThrownBy(() -> hashtagService.getSuggestions("###"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_HASHTAG_INVALID);
        assertThatThrownBy(() -> hashtagService.getSuggestions("do%_an"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_HASHTAG_INVALID);

        verify(hashtagRepository, never()).findSuggestions(anyString());
    }

    private void assertError(ErrorCode errorCode) {
        assertThatThrownBy(() -> hashtagService.getSuggestions("doan"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private User user(Long id, UserStatus status) {
        User user = new User("user" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setStatus(status);
        return user;
    }

    private UserProfile profile(Long id, boolean completed) {
        UserProfile profile = new UserProfile(user(id, UserStatus.ACTIVE));
        ReflectionTestUtils.setField(profile, "userId", id);
        profile.setDisplayName("Nguyen Van A");
        profile.setProfileCompletedAt(completed ? LocalDateTime.of(2026, 7, 16, 10, 0) : null);
        return profile;
    }

    private Hashtag hashtag(Long id, String normalizedName, String displayName, int postCount) {
        Hashtag hashtag = new Hashtag(normalizedName, displayName);
        ReflectionTestUtils.setField(hashtag, "id", id);
        hashtag.setPostCount(postCount);
        return hashtag;
    }
}
