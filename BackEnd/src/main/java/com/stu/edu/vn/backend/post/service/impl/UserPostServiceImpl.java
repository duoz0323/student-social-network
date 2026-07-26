package com.stu.edu.vn.backend.post.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.cursor.TimeCursor;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.service.UserPostService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bảo đảm cả người xem và tác giả hợp lệ trước khi đọc bài PUBLISHED theo keyset. */
@Service
@RequiredArgsConstructor
public class UserPostServiceImpl implements UserPostService {
    private static final int MAX_LIMIT = 20;
    private static final LocalDateTime FIRST_PAGE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final FeedPostBatchLoader feedPostBatchLoader;
    private final CursorCodec cursorCodec;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedPostResponse> getUserPosts(Long userId, String encodedCursor, int limit) {
        if (userId == null || userId <= 0 || limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Long viewerId = currentUserProvider.getCurrentUserId();
        requireActiveCompletedUser(viewerId);
        requireActiveCompletedUser(userId);

        TimeCursor cursor = cursorCodec.decode(encodedCursor, TimeCursor.class);
        if (cursor != null && !cursor.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        LocalDateTime time = cursor == null ? FIRST_PAGE_TIME : cursor.createdAt();
        long postId = cursor == null ? Long.MAX_VALUE : cursor.postId();
        List<Post> fetched = postRepository.findProfilePosts(
                userId, viewerId, time, postId, PageRequest.of(0, limit + 1));
        boolean hasNext = fetched.size() > limit;
        List<Post> posts = fetched.stream().distinct().limit(limit).toList();
        List<FeedPostResponse> content = feedPostBatchLoader.map(posts, viewerId);
        String nextCursor = hasNext && !posts.isEmpty()
                ? cursorCodec.encode(new TimeCursor(
                posts.get(posts.size() - 1).getPublishedAt(), posts.get(posts.size() - 1).getId()))
                : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private void requireActiveCompletedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
    }
}
