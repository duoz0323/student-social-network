package com.stu.edu.vn.backend.search.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.search.dto.response.SearchUserResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.cursor.SearchContentCursor;
import com.stu.edu.vn.backend.search.cursor.SearchHashtagCursor;
import com.stu.edu.vn.backend.search.enums.SearchPostType;
import com.stu.edu.vn.backend.search.mapper.SearchPostMapper;
import com.stu.edu.vn.backend.search.repository.SearchUserProfileRepository;
import com.stu.edu.vn.backend.search.service.SearchService;
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
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Triển khai tìm kiếm hồ sơ theo display name cho người dùng ACTIVE đã hoàn tất onboarding.
 */
@Service
public class SearchServiceImpl implements SearchService {

    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final LocalDateTime FIRST_PAGE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final SearchUserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PostRepostRepository postRepostRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;
    private final SearchPostMapper searchPostMapper;
    private final HashtagNormalizer hashtagNormalizer;
    private final CursorCodec cursorCodec;
    private final PostLocationBatchLoader postLocationBatchLoader;

    public SearchServiceImpl(CurrentUserProvider currentUserProvider, UserRepository userRepository,
                             SearchUserProfileRepository userProfileRepository, PostRepository postRepository,
                             PostMediaRepository postMediaRepository, PostHashtagRepository postHashtagRepository,
                             PostLikeRepository postLikeRepository, SavedPostRepository savedPostRepository,
                             PostRepostRepository postRepostRepository,
                             SearchPostMapper searchPostMapper, HashtagNormalizer hashtagNormalizer,
                             CursorCodec cursorCodec, PostLocationBatchLoader postLocationBatchLoader) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.postHashtagRepository = postHashtagRepository;
        this.postLikeRepository = postLikeRepository;
        this.savedPostRepository = savedPostRepository;
        this.postRepostRepository = postRepostRepository;
        this.searchPostMapper = searchPostMapper;
        this.hashtagNormalizer = hashtagNormalizer;
        this.cursorCodec = cursorCodec;
        this.postLocationBatchLoader = postLocationBatchLoader;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SearchUserResponse> searchUsers(String keyword, int page, int size) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanSearch(currentUserId);
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<UserProfile> profiles = userProfileRepository.searchCompletedActiveProfilesByDisplayName(
                escapeLikePattern(normalizedKeyword), currentUserId, PageRequest.of(page, size));
        Set<Long> followedUserIds = profiles.isEmpty()
                ? Set.of()
                : new HashSet<>(userProfileRepository.findFollowedUserIds(
                        currentUserId,
                        profiles.getContent().stream().map(UserProfile::getUserId).toList()
                ));
        Page<SearchUserResponse> result = profiles.map(profile -> toResponse(
                profile,
                followedUserIds.contains(profile.getUserId())
        ));
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<SearchPostResponse> searchPosts(
            String keyword, SearchPostType type, String encodedCursor, int limit) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanSearch(currentUserId);
        if (type == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (limit < 1 || limit > 20) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String normalizedKeyword = type == SearchPostType.CONTENT
                ? normalizeKeyword(keyword)
                : normalizeHashtag(keyword);

        PageRequest fetchLimit = PageRequest.of(0, limit + 1);
        List<Post> fetched = type == SearchPostType.CONTENT
                ? searchContentPosts(normalizedKeyword, currentUserId, encodedCursor, fetchLimit)
                : searchHashtagPosts(normalizedKeyword, currentUserId, encodedCursor, fetchLimit);
        boolean hasNext = fetched.size() > limit;
        List<Post> posts = hasNext ? fetched.subList(0, limit) : fetched;
        if (posts.isEmpty()) {
            // Không chạy các batch query khi trang rỗng vì không có dữ liệu cần enrichment.
            return new CursorPageResponse<>(List.of(), null, false);
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, UserProfile> authorProfiles = loadAuthorProfiles(posts);
        Map<Long, List<PostMedia>> mediaByPostId = loadMedia(postIds);
        Map<Long, String> hashtagsByPostId = loadHashtags(postIds);
        Set<Long> likedPostIds = new HashSet<>(postLikeRepository.findLikedPostIds(currentUserId, postIds));
        Set<Long> savedPostIds = new HashSet<>(savedPostRepository.findSavedPostIds(currentUserId, postIds));
        Set<Long> repostedPostIds = new HashSet<>(postRepostRepository.findRepostedPostIds(currentUserId, postIds));
        Map<Long, Location> locations = postLocationBatchLoader.loadByPostId(posts);

        List<SearchPostResponse> content = posts.stream().map(post -> searchPostMapper.toResponse(
                post,
                authorProfiles.get(post.getAuthor().getId()),
                mediaByPostId.getOrDefault(post.getId(), List.of()),
                hashtagsByPostId.get(post.getId()),
                likedPostIds.contains(post.getId()),
                savedPostIds.contains(post.getId()),
                repostedPostIds.contains(post.getId()),
                locations.get(post.getId())
        )).toList();
        String nextCursor = hasNext ? createSearchPostCursor(type, normalizedKeyword, posts.getLast()) : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private List<Post> searchContentPosts(String keyword, Long currentUserId, String encodedCursor,
                                          PageRequest fetchLimit) {
        SearchContentCursor cursor = cursorCodec.decode(encodedCursor, SearchContentCursor.class);
        if (cursor != null && !cursor.isValidFor(keyword)) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        return postRepository.searchPublishedPostsByContentAfter(
                keyword,
                currentUserId,
                cursor == null ? null : cursor.relevance(),
                cursor == null ? FIRST_PAGE_TIME : cursor.publishedAt(),
                cursor == null ? Long.MAX_VALUE : cursor.postId(),
                fetchLimit
        );
    }

    private List<Post> searchHashtagPosts(String hashtag, Long currentUserId, String encodedCursor,
                                          PageRequest fetchLimit) {
        SearchHashtagCursor cursor = cursorCodec.decode(encodedCursor, SearchHashtagCursor.class);
        if (cursor != null && !cursor.isValidFor(hashtag)) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        return postRepository.searchPublishedPostsByHashtagAfter(
                hashtag,
                currentUserId,
                cursor == null ? FIRST_PAGE_TIME : cursor.publishedAt(),
                cursor == null ? Long.MAX_VALUE : cursor.postId(),
                fetchLimit
        );
    }

    private String createSearchPostCursor(SearchPostType type, String keyword, Post lastPost) {
        if (type == SearchPostType.HASHTAG) {
            return cursorCodec.encode(new SearchHashtagCursor(keyword, lastPost.getPublishedAt(), lastPost.getId()));
        }
        Double relevance = postRepository.findContentSearchRelevance(lastPost.getId(), keyword)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
        return cursorCodec.encode(new SearchContentCursor(keyword, relevance, lastPost.getPublishedAt(), lastPost.getId()));
    }

    private Map<Long, UserProfile> loadAuthorProfiles(List<Post> posts) {
        List<Long> authorIds = posts.stream().map(post -> post.getAuthor().getId()).distinct().toList();
        // Batch riêng hồ sơ tác giả vì native pageable query không bảo đảm EntityGraph được áp dụng.
        return userProfileRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
    }

    private Map<Long, List<PostMedia>> loadMedia(List<Long> postIds) {
        Map<Long, List<PostMedia>> result = new HashMap<>();
        for (PostMedia media : postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(postIds)) {
            result.computeIfAbsent(media.getPost().getId(), ignored -> new ArrayList<>()).add(media);
        }
        return result;
    }

    private Map<Long, String> loadHashtags(List<Long> postIds) {
        Map<Long, String> result = new HashMap<>();
        for (PostHashtag relation : postHashtagRepository.findWithHashtagByPostIds(postIds)) {
            String previous = result.put(relation.getPost().getId(), relation.getHashtag().getNormalizedName());
            if (previous != null) {
                // Không che giấu dữ liệu cũ vi phạm invariant một hashtag cho mỗi bài viết.
                throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
        }
        return result;
    }

    private String normalizeHashtag(String keyword) {
        try {
            String normalized = hashtagNormalizer.normalizeOptional(keyword);
            if (normalized == null) {
                throw new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED);
            }
            return normalized;
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.POST_HASHTAG_INVALID) {
                throw new BusinessException(ErrorCode.SEARCH_HASHTAG_INVALID);
            }
            if (exception.getErrorCode() == ErrorCode.POST_HASHTAG_TOO_LONG) {
                throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_LONG);
            }
            throw exception;
        }
    }

    private void ensureCurrentUserCanSearch(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        UserProfile profile = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED);
        }
        String normalizedKeyword = keyword.trim();
        if (normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_LONG);
        }
        return normalizedKeyword;
    }

    private String escapeLikePattern(String keyword) {
        // Dùng cùng quy tắc escape LIKE với các module có hỗ trợ tìm kiếm khác.
        return LikePatternEscaper.escape(keyword);
    }

    private SearchUserResponse toResponse(UserProfile profile, boolean followedByCurrentUser) {
        return new SearchUserResponse(
                profile.getUserId(), profile.getDisplayName(), profile.getAvatarUrl(), profile.getBio(),
                followedByCurrentUser
        );
    }
}
