package com.stu.edu.vn.backend.search.service.impl;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.search.dto.response.SearchUserResponse;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.enums.SearchPostType;
import com.stu.edu.vn.backend.search.mapper.SearchPostMapper;
import com.stu.edu.vn.backend.search.service.SearchService;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Triển khai tìm kiếm hồ sơ theo display name cho người dùng ACTIVE đã hoàn tất onboarding.
 */
@Service
public class SearchServiceImpl implements SearchService {

    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_]+$");

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;
    private final SearchPostMapper searchPostMapper;

    public SearchServiceImpl(CurrentUserProvider currentUserProvider, UserRepository userRepository,
                             UserProfileRepository userProfileRepository, PostRepository postRepository,
                             PostMediaRepository postMediaRepository, PostHashtagRepository postHashtagRepository,
                             PostLikeRepository postLikeRepository, SavedPostRepository savedPostRepository,
                             SearchPostMapper searchPostMapper) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.postHashtagRepository = postHashtagRepository;
        this.postLikeRepository = postLikeRepository;
        this.savedPostRepository = savedPostRepository;
        this.searchPostMapper = searchPostMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SearchUserResponse> searchUsers(String keyword, int page, int size) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanSearch(currentUserId);
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<SearchUserResponse> result = userProfileRepository
                .searchCompletedActiveProfilesByDisplayName(escapeLikePattern(normalizedKeyword), PageRequest.of(page, size))
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SearchPostResponse> searchPosts(String keyword, SearchPostType type, int page, int size) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanSearch(currentUserId);
        String normalizedKeyword = normalizeKeyword(keyword);
        if (type == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<Post> posts = type == SearchPostType.CONTENT
                ? postRepository.searchPublishedPostsByContent(normalizedKeyword, pageable)
                : postRepository.searchPublishedPostsByHashtag(normalizeHashtag(normalizedKeyword), pageable);
        if (posts.isEmpty()) {
            // Không chạy các batch query khi trang rỗng vì không có dữ liệu cần enrichment.
            return new PageResponse<>(List.of(), posts.getNumber(), posts.getSize(), posts.getTotalElements(),
                    posts.getTotalPages(), posts.isFirst(), posts.isLast());
        }

        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, UserProfile> authorProfiles = loadAuthorProfiles(posts.getContent());
        Map<Long, List<PostMedia>> mediaByPostId = loadMedia(postIds);
        Map<Long, List<String>> hashtagsByPostId = loadHashtags(postIds);
        Set<Long> likedPostIds = new HashSet<>(postLikeRepository.findLikedPostIds(currentUserId, postIds));
        Set<Long> savedPostIds = new HashSet<>(savedPostRepository.findSavedPostIds(currentUserId, postIds));

        Page<SearchPostResponse> result = posts.map(post -> searchPostMapper.toResponse(
                post,
                authorProfiles.get(post.getAuthor().getId()),
                mediaByPostId.getOrDefault(post.getId(), List.of()),
                hashtagsByPostId.getOrDefault(post.getId(), List.of()),
                likedPostIds.contains(post.getId()),
                savedPostIds.contains(post.getId())
        ));
        return PageResponse.from(result);
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

    private Map<Long, List<String>> loadHashtags(List<Long> postIds) {
        Map<Long, List<String>> result = new HashMap<>();
        for (PostHashtag relation : postHashtagRepository.findWithHashtagByPostIds(postIds)) {
            result.computeIfAbsent(relation.getPost().getId(), ignored -> new ArrayList<>())
                    .add(relation.getHashtag().getNormalizedName());
        }
        return result;
    }

    private String normalizeHashtag(String keyword) {
        int firstNonHash = 0;
        while (firstNonHash < keyword.length() && keyword.charAt(firstNonHash) == '#') {
            firstNonHash++;
        }
        String normalized = keyword.substring(firstNonHash).toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || !HASHTAG_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCode.SEARCH_HASHTAG_INVALID);
        }
        return normalized;
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

    private SearchUserResponse toResponse(UserProfile profile) {
        return new SearchUserResponse(profile.getUserId(), profile.getDisplayName(), profile.getAvatarUrl(), profile.getBio());
    }
}
