package com.stu.edu.vn.backend.search.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.search.enums.SearchPostType;
import com.stu.edu.vn.backend.search.mapper.SearchPostMapper;
import com.stu.edu.vn.backend.search.repository.SearchUserProfileRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

class SearchPostServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final SearchUserProfileRepository userProfileRepository = org.mockito.Mockito.mock(SearchUserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final PostMediaRepository postMediaRepository = org.mockito.Mockito.mock(PostMediaRepository.class);
    private final PostHashtagRepository postHashtagRepository = org.mockito.Mockito.mock(PostHashtagRepository.class);
    private final PostLikeRepository postLikeRepository = org.mockito.Mockito.mock(PostLikeRepository.class);
    private final SavedPostRepository savedPostRepository = org.mockito.Mockito.mock(SavedPostRepository.class);
    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchServiceImpl(
                currentUserProvider, userRepository, userProfileRepository, postRepository, postMediaRepository,
                postHashtagRepository, postLikeRepository, savedPostRepository, new SearchPostMapper(),
                new HashtagNormalizer());
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile(10L, "Current User", true)));
    }

    @Test
    void contentSearchBatchLoadsAndMapsPostCardStatesWithoutNPlusOne() {
        User author = user(20L, UserStatus.ACTIVE);
        UserProfile authorProfile = profile(20L, "Nguyễn Minh", true);
        Post post = post(100L, author, "Học Java");
        PostMedia second = media(post, 2L, 1);
        PostMedia first = media(post, 1L, 0);
        Hashtag hashtag = hashtag(50L, "java");
        PageRequest pageable = PageRequest.of(0, 20);

        when(postRepository.searchPublishedPostsByContent("Học Java", pageable))
                .thenReturn(new PageImpl<>(List.of(post), pageable, 1));
        when(userProfileRepository.findAllById(List.of(20L))).thenReturn(List.of(authorProfile));
        when(postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(List.of(100L)))
                .thenReturn(List.of(first, second));
        when(postHashtagRepository.findWithHashtagByPostIds(List.of(100L)))
                .thenReturn(List.of(new PostHashtag(post, hashtag)));
        when(postLikeRepository.findLikedPostIds(10L, List.of(100L))).thenReturn(List.of(100L));
        when(savedPostRepository.findSavedPostIds(10L, List.of(100L))).thenReturn(List.of(100L));

        var response = searchService.searchPosts("  Học Java  ", SearchPostType.CONTENT, 0, 20);

        assertThat(response.content()).hasSize(1);
        var item = response.content().getFirst();
        assertThat(item.postId()).isEqualTo(100L);
        assertThat(item.author().id()).isEqualTo(20L);
        assertThat(item.media()).extracting("displayOrder").containsExactly(0, 1);
        assertThat(item.hashtag()).isEqualTo("java");
        assertThat(item.likedByCurrentUser()).isTrue();
        assertThat(item.savedByCurrentUser()).isTrue();
        verify(postMediaRepository, times(1)).findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(any());
        verify(postHashtagRepository, times(1)).findWithHashtagByPostIds(any());
        verify(postLikeRepository, times(1)).findLikedPostIds(any(), any());
        verify(savedPostRepository, times(1)).findSavedPostIds(any(), any());
        verify(postLikeRepository, never()).existsByIdUserIdAndIdPostId(any(), any());
        verify(savedPostRepository, never()).existsByIdUserIdAndIdPostId(any(), any());
    }

    @Test
    void hashtagSearchNormalizesLeadingHashesAndLocaleRootLowercase() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(postRepository.searchPublishedPostsByHashtag("sinhvien", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(postRepository.searchPublishedPostsByHashtag("java", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(postRepository.searchPublishedPostsByHashtag("hoctap", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        searchService.searchPosts("#SinhVien", SearchPostType.HASHTAG, 0, 20);
        searchService.searchPosts(" #Java ", SearchPostType.HASHTAG, 0, 20);
        searchService.searchPosts("###HocTap", SearchPostType.HASHTAG, 0, 20);

        verify(postRepository).searchPublishedPostsByHashtag("sinhvien", pageable);
        verify(postRepository).searchPublishedPostsByHashtag("java", pageable);
        verify(postRepository).searchPublishedPostsByHashtag("hoctap", pageable);
    }

    @Test
    void invalidHashtagIsRejectedBeforeRepositoryQuery() {
        assertError(() -> searchService.searchPosts("###", SearchPostType.HASHTAG, 0, 20), ErrorCode.SEARCH_HASHTAG_INVALID);
        assertError(() -> searchService.searchPosts("#java!", SearchPostType.HASHTAG, 0, 20), ErrorCode.SEARCH_HASHTAG_INVALID);
        verify(postRepository, never()).searchPublishedPostsByHashtag(any(), any());
    }

    @Test
    void emptyPageSkipsAllBatchQueries() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(postRepository.searchPublishedPostsByContent("java", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = searchService.searchPosts("java", SearchPostType.CONTENT, 0, 20);

        assertThat(response.content()).isEmpty();
        verify(userProfileRepository, never()).findAllById(any());
        verify(postMediaRepository, never()).findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(any());
        verify(postHashtagRepository, never()).findWithHashtagByPostIds(any());
        verify(postLikeRepository, never()).findLikedPostIds(any(), any());
        verify(savedPostRepository, never()).findSavedPostIds(any(), any());
    }

    @Test
    void commonKeywordAndCurrentUserPreconditionsApplyToPostSearch() {
        assertError(() -> searchService.searchPosts(null, SearchPostType.CONTENT, 0, 20), ErrorCode.SEARCH_KEYWORD_REQUIRED);
        assertError(() -> searchService.searchPosts("   ", SearchPostType.CONTENT, 0, 20), ErrorCode.SEARCH_KEYWORD_REQUIRED);
        assertError(() -> searchService.searchPosts("a".repeat(101), SearchPostType.CONTENT, 0, 20), ErrorCode.SEARCH_KEYWORD_TOO_LONG);
        assertError(() -> searchService.searchPosts("java", null, 0, 20), ErrorCode.VALIDATION_ERROR);

        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, 0, 20), ErrorCode.USER_NOT_FOUND);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.BLOCKED)));
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, 0, 20), ErrorCode.USER_BLOCKED);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, 0, 20), ErrorCode.PROFILE_NOT_FOUND);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile(10L, "Current", false)));
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, 0, 20), ErrorCode.PROFILE_NOT_COMPLETED);
    }

    private void assertError(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(code);
    }

    private User user(Long id, UserStatus status) {
        User user = new User("u" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setStatus(status);
        return user;
    }

    private UserProfile profile(Long id, String name, boolean completed) {
        UserProfile profile = new UserProfile(user(id, UserStatus.ACTIVE));
        ReflectionTestUtils.setField(profile, "userId", id);
        profile.setDisplayName(name);
        profile.setProfileCompletedAt(completed ? LocalDateTime.of(2026, 7, 13, 1, 0) : null);
        return profile;
    }

    private Post post(Long id, User author, String content) {
        Post post = new Post(author, content);
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "publishedAt", LocalDateTime.of(2026, 7, 13, 1, 0));
        post.setLikeCount(3);
        post.setCommentCount(2);
        return post;
    }

    private PostMedia media(Post post, Long id, int order) {
        PostMedia media = new PostMedia(post, "https://cdn.example/" + id, "post/" + id, "image/png", 10L, order);
        ReflectionTestUtils.setField(media, "id", id);
        return media;
    }

    private Hashtag hashtag(Long id, String name) {
        Hashtag hashtag = new Hashtag(name, name);
        ReflectionTestUtils.setField(hashtag, "id", id);
        return hashtag;
    }
}
