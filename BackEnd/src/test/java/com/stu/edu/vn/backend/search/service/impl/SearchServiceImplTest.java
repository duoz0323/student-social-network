package com.stu.edu.vn.backend.search.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.search.mapper.SearchPostMapper;
import com.stu.edu.vn.backend.search.repository.SearchUserProfileRepository;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class SearchServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final SearchUserProfileRepository userProfileRepository = org.mockito.Mockito.mock(SearchUserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final PostMediaRepository postMediaRepository = org.mockito.Mockito.mock(PostMediaRepository.class);
    private final PostHashtagRepository postHashtagRepository = org.mockito.Mockito.mock(PostHashtagRepository.class);
    private final PostLikeRepository postLikeRepository = org.mockito.Mockito.mock(PostLikeRepository.class);
    private final SavedPostRepository savedPostRepository = org.mockito.Mockito.mock(SavedPostRepository.class);
    private final CursorCodec cursorCodec = org.mockito.Mockito.mock(CursorCodec.class);
    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchServiceImpl(
                currentUserProvider, userRepository, userProfileRepository, postRepository, postMediaRepository,
                postHashtagRepository, postLikeRepository, savedPostRepository, new SearchPostMapper(),
                new HashtagNormalizer(), cursorCodec);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile(10L, "Current User", true)));
    }

    @Test
    void searchUsesCurrentUserTrimsKeywordAndMapsOnlyPublicFields() {
        UserProfile resultProfile = profile(20L, "Nguyễn Minh", true);
        resultProfile.setAvatarUrl("https://cdn.example/avatar.png");
        resultProfile.setBio("Sinh viên CNTT");
        when(userProfileRepository.searchCompletedActiveProfilesByDisplayName(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(resultProfile), PageRequest.of(0, 20), 1));
        when(userProfileRepository.findFollowedUserIds(10L, List.of(20L))).thenReturn(List.of(20L));

        var response = searchService.searchUsers("  Minh  ", 0, 20);

        verify(currentUserProvider).getCurrentUserId();
        verify(userRepository).findById(10L);
        verify(userProfileRepository).searchCompletedActiveProfilesByDisplayName("Minh", 10L, PageRequest.of(0, 20));
        verify(userProfileRepository).findFollowedUserIds(10L, List.of(20L));
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst())
                .extracting("userId", "displayName", "avatarUrl", "bio", "followedByCurrentUser")
                .containsExactly(20L, "Nguyễn Minh", "https://cdn.example/avatar.png", "Sinh viên CNTT", true);
    }

    @Test
    void searchEscapesLikeWildcardCharacters() {
        when(userProfileRepository.searchCompletedActiveProfilesByDisplayName(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        searchService.searchUsers("50%_off=now", 0, 20);

        verify(userProfileRepository).searchCompletedActiveProfilesByDisplayName(
                "50=%=_off==now", 10L, PageRequest.of(0, 20));
    }

    @Test
    void emptyResultReturnsEmptyPageResponse() {
        when(userProfileRepository.searchCompletedActiveProfilesByDisplayName(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 0));

        var response = searchService.searchUsers("không tồn tại", 1, 10);

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isZero();
    }

    @Test
    void rejectsMissingBlankAndTooLongKeywordsWithoutSearchQuery() {
        assertError(() -> searchService.searchUsers(null, 0, 20), ErrorCode.SEARCH_KEYWORD_REQUIRED);
        assertError(() -> searchService.searchUsers("   ", 0, 20), ErrorCode.SEARCH_KEYWORD_REQUIRED);
        assertError(() -> searchService.searchUsers("a".repeat(101), 0, 20), ErrorCode.SEARCH_KEYWORD_TOO_LONG);
        verify(userProfileRepository, never()).searchCompletedActiveProfilesByDisplayName(any(), any(), any());
    }

    @Test
    void rejectsMissingBlockedOrIncompleteCurrentUserBeforeSearchQuery() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(() -> searchService.searchUsers("minh", 0, 20), ErrorCode.USER_NOT_FOUND);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.BLOCKED)));
        assertError(() -> searchService.searchUsers("minh", 0, 20), ErrorCode.USER_BLOCKED);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(() -> searchService.searchUsers("minh", 0, 20), ErrorCode.PROFILE_NOT_FOUND);

        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile(10L, "Current User", false)));
        assertError(() -> searchService.searchUsers("minh", 0, 20), ErrorCode.PROFILE_NOT_COMPLETED);
        verify(userProfileRepository, never()).searchCompletedActiveProfilesByDisplayName(any(), any(), any());
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(errorCode);
    }

    private User user(Long id, UserStatus status) {
        User user = new User("user" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setStatus(status);
        return user;
    }

    private UserProfile profile(Long id, String displayName, boolean completed) {
        UserProfile profile = new UserProfile(user(id, UserStatus.ACTIVE));
        ReflectionTestUtils.setField(profile, "userId", id);
        profile.setDisplayName(displayName);
        profile.setProfileCompletedAt(completed ? LocalDateTime.of(2026, 7, 13, 10, 0) : null);
        return profile;
    }
}
