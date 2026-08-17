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
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.search.cursor.SearchContentCursor;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostRepostRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.post.service.PostLocationBatchLoader;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

class SearchPostServiceImplTest {

    private static final LocalDateTime FIRST_PAGE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final SearchUserProfileRepository userProfileRepository = org.mockito.Mockito.mock(SearchUserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final PostMediaRepository postMediaRepository = org.mockito.Mockito.mock(PostMediaRepository.class);
    private final PostHashtagRepository postHashtagRepository = org.mockito.Mockito.mock(PostHashtagRepository.class);
    private final PostLikeRepository postLikeRepository = org.mockito.Mockito.mock(PostLikeRepository.class);
    private final SavedPostRepository savedPostRepository = org.mockito.Mockito.mock(SavedPostRepository.class);
    private final CursorCodec cursorCodec = new CursorCodec(new ObjectMapper());
    private final PostRepostRepository postRepostRepository = org.mockito.Mockito.mock(PostRepostRepository.class);
    private final PostLocationBatchLoader postLocationBatchLoader = org.mockito.Mockito.mock(PostLocationBatchLoader.class);
    private final com.stu.edu.vn.backend.user.service.PublicUserBadgeService badgeService =
            org.mockito.Mockito.mock(com.stu.edu.vn.backend.user.service.PublicUserBadgeService.class);
    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchServiceImpl(
                currentUserProvider, userRepository, userProfileRepository, postRepository, postMediaRepository,
                postHashtagRepository, postLikeRepository, savedPostRepository, postRepostRepository,
                new SearchPostMapper(),
                new HashtagNormalizer(), cursorCodec, postLocationBatchLoader, badgeService);
        when(badgeService.getBadgesByUserIds(any())).thenReturn(java.util.Map.of());
        when(postLocationBatchLoader.loadByPostId(any())).thenReturn(java.util.Map.of());
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
        PageRequest fetchLimit = PageRequest.of(0, 21);

        when(postRepository.searchPublishedPostsByContentAfter(
                "Học Java", 10L, null, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit))
                .thenReturn(List.of(post));
        when(userProfileRepository.findAllById(List.of(20L))).thenReturn(List.of(authorProfile));
        when(postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(List.of(100L)))
                .thenReturn(List.of(first, second));
        when(postHashtagRepository.findWithHashtagByPostIds(List.of(100L)))
                .thenReturn(List.of(new PostHashtag(post, hashtag)));
        when(postLikeRepository.findLikedPostIds(10L, List.of(100L))).thenReturn(List.of(100L));
        when(savedPostRepository.findSavedPostIds(10L, List.of(100L))).thenReturn(List.of(100L));
        when(postRepostRepository.findRepostedPostIds(10L, List.of(100L))).thenReturn(List.of(100L));

        var response = searchService.searchPosts("  Học Java  ", SearchPostType.CONTENT, null, 20);

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
        PageRequest fetchLimit = PageRequest.of(0, 21);
        when(postRepository.searchPublishedPostsByHashtagAfter("sinhvien", 10L, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit))
                .thenReturn(List.of());
        when(postRepository.searchPublishedPostsByHashtagAfter("java", 10L, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit))
                .thenReturn(List.of());
        when(postRepository.searchPublishedPostsByHashtagAfter("hoctap", 10L, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit))
                .thenReturn(List.of());

        searchService.searchPosts("#SinhVien", SearchPostType.HASHTAG, null, 20);
        searchService.searchPosts(" #Java ", SearchPostType.HASHTAG, null, 20);
        searchService.searchPosts("###HocTap", SearchPostType.HASHTAG, null, 20);

        verify(postRepository).searchPublishedPostsByHashtagAfter("sinhvien", 10L, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit);
        verify(postRepository).searchPublishedPostsByHashtagAfter("java", 10L, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit);
        verify(postRepository).searchPublishedPostsByHashtagAfter("hoctap", 10L, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit);
    }

    @Test
    void invalidHashtagIsRejectedBeforeRepositoryQuery() {
        assertError(() -> searchService.searchPosts("###", SearchPostType.HASHTAG, null, 20), ErrorCode.SEARCH_HASHTAG_INVALID);
        assertError(() -> searchService.searchPosts("#java!", SearchPostType.HASHTAG, null, 20), ErrorCode.SEARCH_HASHTAG_INVALID);
        verify(postRepository, never()).searchPublishedPostsByHashtagAfter(any(), any(), any(), any(), any());
    }

    @Test
    void emptyPageSkipsAllBatchQueries() {
        PageRequest fetchLimit = PageRequest.of(0, 21);
        when(postRepository.searchPublishedPostsByContentAfter(
                "java", 10L, null, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit))
                .thenReturn(List.of());

        var response = searchService.searchPosts("java", SearchPostType.CONTENT, null, 20);

        assertThat(response.content()).isEmpty();
        verify(userProfileRepository, never()).findAllById(any());
        verify(postMediaRepository, never()).findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(any());
        verify(postHashtagRepository, never()).findWithHashtagByPostIds(any());
        verify(postLikeRepository, never()).findLikedPostIds(any(), any());
        verify(savedPostRepository, never()).findSavedPostIds(any(), any());
    }

    @Test
    void commonKeywordAndCurrentUserPreconditionsApplyToPostSearch() {
        assertError(() -> searchService.searchPosts(null, SearchPostType.CONTENT, null, 20), ErrorCode.SEARCH_KEYWORD_REQUIRED);
        assertError(() -> searchService.searchPosts("   ", SearchPostType.CONTENT, null, 20), ErrorCode.SEARCH_KEYWORD_REQUIRED);
        assertError(() -> searchService.searchPosts("a".repeat(101), SearchPostType.CONTENT, null, 20), ErrorCode.SEARCH_KEYWORD_TOO_LONG);
        assertError(() -> searchService.searchPosts("java", null, null, 20), ErrorCode.VALIDATION_ERROR);
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, null, 21), ErrorCode.VALIDATION_ERROR);

        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, null, 20), ErrorCode.USER_NOT_FOUND);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.BLOCKED)));
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, null, 20), ErrorCode.USER_BLOCKED);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.empty());
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, null, 20), ErrorCode.PROFILE_NOT_FOUND);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile(10L, "Current", false)));
        assertError(() -> searchService.searchPosts("java", SearchPostType.CONTENT, null, 20), ErrorCode.PROFILE_NOT_COMPLETED);
    }

    @Test
    void contentSearchCreatesStableCursorFromLastVisiblePost() {
        User author = user(20L, UserStatus.ACTIVE);
        Post first = post(100L, author, "Java one");
        Post lookahead = post(99L, author, "Java two");
        PageRequest fetchLimit = PageRequest.of(0, 2);
        when(postRepository.searchPublishedPostsByContentAfter(
                "java", 10L, null, FIRST_PAGE_TIME, Long.MAX_VALUE, fetchLimit))
                .thenReturn(List.of(first, lookahead));
        when(userProfileRepository.findAllById(List.of(20L))).thenReturn(List.of(profile(20L, "Minh", true)));
        when(postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(List.of(100L))).thenReturn(List.of());
        when(postHashtagRepository.findWithHashtagByPostIds(List.of(100L))).thenReturn(List.of());
        when(postLikeRepository.findLikedPostIds(10L, List.of(100L))).thenReturn(List.of());
        when(savedPostRepository.findSavedPostIds(10L, List.of(100L))).thenReturn(List.of());
        when(postRepository.findContentSearchRelevance(100L, "java")).thenReturn(Optional.of(1.25));

        var response = searchService.searchPosts("java", SearchPostType.CONTENT, null, 1);
        SearchContentCursor cursor = cursorCodec.decode(response.nextCursor(), SearchContentCursor.class);

        assertThat(response.content()).extracting("postId").containsExactly(100L);
        assertThat(response.hasNext()).isTrue();
        assertThat(cursor.keyword()).isEqualTo("java");
        assertThat(cursor.relevance()).isEqualTo(1.25);
        assertThat(cursor.postId()).isEqualTo(100L);
    }

    @Test
    void cursorFromAnotherKeywordIsRejected() {
        String cursor = cursorCodec.encode(new SearchContentCursor(
                "spring", 1.25, LocalDateTime.of(2026, 7, 13, 1, 0), 100L));

        assertError(
                () -> searchService.searchPosts("java", SearchPostType.CONTENT, cursor, 10),
                ErrorCode.INVALID_CURSOR
        );
        verify(postRepository, never()).searchPublishedPostsByContentAfter(
                any(), any(), any(), any(), any(), any());
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
