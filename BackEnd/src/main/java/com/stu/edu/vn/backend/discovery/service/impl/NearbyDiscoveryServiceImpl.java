package com.stu.edu.vn.backend.discovery.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.discovery.cursor.NearbyCursor;
import com.stu.edu.vn.backend.discovery.dto.response.NearbyPostItemResponse;
import com.stu.edu.vn.backend.discovery.model.NearbyBoundingBox;
import com.stu.edu.vn.backend.discovery.repository.NearbyDiscoveryRepository;
import com.stu.edu.vn.backend.discovery.repository.NearbyPostRank;
import com.stu.edu.vn.backend.discovery.service.NearbyDiscoveryService;
import com.stu.edu.vn.backend.discovery.service.NearbyQuerySupport;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Điều phối Nearby read-only, giữ tọa độ trong phạm vi request và không ghi vào bất kỳ storage nào. */
@Service
@RequiredArgsConstructor
public class NearbyDiscoveryServiceImpl implements NearbyDiscoveryService {
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileRepository userProfileRepository;
    private final NearbyQuerySupport querySupport;
    private final CursorCodec cursorCodec;
    private final NearbyDiscoveryRepository nearbyDiscoveryRepository;
    private final PostRepository postRepository;
    private final FeedPostBatchLoader feedPostBatchLoader;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<NearbyPostItemResponse> getNearby(
            double latitude,
            double longitude,
            int radiusKm,
            int limit,
            String encodedCursor
    ) {
        querySupport.validate(latitude, longitude, radiusKm, limit);
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        ensureEligibleViewer(principal);

        String fingerprint = querySupport.fingerprint(latitude, longitude, radiusKm);
        NearbyCursor cursor = cursorCodec.decode(encodedCursor, NearbyCursor.class);
        if (cursor != null && (!cursor.isValid() || !fingerprint.equals(cursor.queryFingerprint()))) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }

        NearbyBoundingBox boundingBox = querySupport.boundingBox(latitude, longitude, radiusKm);
        List<NearbyPostRank> fetched = nearbyDiscoveryRepository.findNearby(
                principal.getUserId(), latitude, longitude, radiusKm, boundingBox, cursor, limit + 1);
        boolean hasNext = fetched.size() > limit;
        List<NearbyPostRank> pageRanks = fetched.stream().limit(limit).toList();
        if (pageRanks.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null, false);
        }

        List<Long> postIds = pageRanks.stream().map(NearbyPostRank::postId).toList();
        Map<Long, Post> postsById = postRepository.findFeedHeadersByIds(postIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        List<Post> orderedPosts = postIds.stream().map(postId -> requirePost(postsById, postId)).toList();
        List<FeedPostResponse> postResponses = feedPostBatchLoader.map(orderedPosts, principal.getUserId());
        List<NearbyPostItemResponse> content = java.util.stream.IntStream.range(0, pageRanks.size())
                .mapToObj(index -> new NearbyPostItemResponse(
                        postResponses.get(index), pageRanks.get(index).distanceMeters()))
                .toList();

        NearbyPostRank lastReturned = pageRanks.getLast();
        String nextCursor = hasNext
                ? cursorCodec.encode(new NearbyCursor(
                        NearbyCursor.CURRENT_VERSION,
                        lastReturned.distanceMeters(),
                        lastReturned.publishedAt(),
                        lastReturned.postId(),
                        fingerprint))
                : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private void ensureEligibleViewer(CustomUserPrincipal principal) {
        if (principal.getRole() != UserRole.USER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (principal.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        if (!userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(principal.getUserId())) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
    }

    private Post requirePost(Map<Long, Post> postsById, Long postId) {
        Post post = postsById.get(postId);
        if (post == null) {
            // Trong cùng read transaction, candidate và batch header phải thuộc cùng một snapshot nhất quán.
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        return post;
    }
}
