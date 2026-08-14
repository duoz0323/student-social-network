package com.stu.edu.vn.backend.discovery.service.impl;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.discovery.config.DiscoveryMapProperties;
import com.stu.edu.vn.backend.discovery.cursor.MapLocationPostsCursor;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationResponse;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationsResponse;
import com.stu.edu.vn.backend.discovery.repository.DiscoveryMapRepository;
import com.stu.edu.vn.backend.discovery.repository.MapLocationPostKey;
import com.stu.edu.vn.backend.discovery.security.DiscoveryViewerGuard;
import com.stu.edu.vn.backend.discovery.service.DiscoveryMapQuerySupport;
import com.stu.edu.vn.backend.discovery.service.DiscoveryMapService;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Điều phối hai read model Map mà không tạo state hoặc Post-loading stack riêng. */
@Service
@RequiredArgsConstructor
public class DiscoveryMapServiceImpl implements DiscoveryMapService {
    private final DiscoveryViewerGuard viewerGuard;
    private final DiscoveryMapQuerySupport querySupport;
    private final DiscoveryMapProperties properties;
    private final CursorCodec cursorCodec;
    private final DiscoveryMapRepository repository;
    private final PostRepository postRepository;
    private final FeedPostBatchLoader feedPostBatchLoader;

    @Override
    @Transactional(readOnly = true)
    public MapLocationsResponse getLocations(double north, double south, double east, double west) {
        querySupport.validateViewport(north, south, east, west);
        CustomUserPrincipal principal = viewerGuard.requireEligibleViewer();
        int maxLocations = properties.getMaxLocations();
        if (maxLocations < 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        List<MapLocationResponse> fetched = repository.findMapLocations(
                principal.getUserId(), north, south, east, west, maxLocations + 1);
        boolean truncated = fetched.size() > maxLocations;
        return new MapLocationsResponse(fetched.stream().limit(maxLocations).toList(), truncated);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedPostResponse> getLocationPosts(
            Long locationId,
            int limit,
            String encodedCursor
    ) {
        querySupport.validateLocationPosts(locationId, limit);
        CustomUserPrincipal principal = viewerGuard.requireEligibleViewer();
        MapLocationPostsCursor cursor = cursorCodec.decode(encodedCursor, MapLocationPostsCursor.class);
        if (cursor != null && !cursor.isValidFor(locationId)) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }

        List<MapLocationPostKey> fetched = repository.findLocationPostKeys(
                principal.getUserId(), locationId, cursor, limit + 1);
        boolean hasNext = fetched.size() > limit;
        List<MapLocationPostKey> pageKeys = fetched.stream().limit(limit).toList();
        if (pageKeys.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null, false);
        }

        List<Long> postIds = pageKeys.stream().map(MapLocationPostKey::postId).toList();
        Map<Long, Post> postsById = postRepository.findFeedHeadersByIds(postIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        List<Post> orderedPosts = postIds.stream().map(postId -> requirePost(postsById, postId)).toList();
        List<FeedPostResponse> content = feedPostBatchLoader.map(orderedPosts, principal.getUserId());

        MapLocationPostKey lastReturned = pageKeys.getLast();
        String nextCursor = hasNext
                ? cursorCodec.encode(new MapLocationPostsCursor(
                        MapLocationPostsCursor.CURRENT_VERSION,
                        locationId,
                        lastReturned.publishedAt(),
                        lastReturned.postId()))
                : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private Post requirePost(Map<Long, Post> postsById, Long postId) {
        Post post = postsById.get(postId);
        if (post == null) {
            // Candidate và batch header phải cùng tồn tại trong snapshot read-only hiện tại.
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        return post;
    }
}
