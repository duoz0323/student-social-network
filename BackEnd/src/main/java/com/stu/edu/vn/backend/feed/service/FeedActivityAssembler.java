package com.stu.edu.vn.backend.feed.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.enums.FeedItemType;
import com.stu.edu.vn.backend.post.dto.response.PostAuthorResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.projection.FeedActivityProjection;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Batch-load bài gốc và người Repost cho cả trang hoạt động, không query trong vòng lặp. */
@Component
@RequiredArgsConstructor
public class FeedActivityAssembler {
    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;
    private final FeedPostBatchLoader feedPostBatchLoader;

    public List<FeedItemResponse> assemble(List<FeedActivityProjection> activities, Long viewerId) {
        if (activities.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = activities.stream().map(FeedActivityProjection::getPostId).distinct().toList();
        Map<Long, Post> postsById = postRepository.findFeedHeadersByIds(postIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        List<Post> orderedPosts = activities.stream().map(activity -> {
            Post post = postsById.get(activity.getPostId());
            if (post == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
            return post;
        }).toList();
        List<FeedPostResponse> postResponses = feedPostBatchLoader.map(orderedPosts, viewerId);

        List<Long> reposterIds = activities.stream()
                .filter(activity -> activity.getItemRank() == 1)
                .map(FeedActivityProjection::getActorId)
                .distinct()
                .toList();
        Map<Long, UserProfile> reposters = reposterIds.isEmpty()
                ? Map.of()
                : userProfileRepository.findAllById(reposterIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        Map<Long, PostAuthorResponse> reposterResponses = new HashMap<>();
        reposters.forEach((userId, profile) -> reposterResponses.put(userId,
                new PostAuthorResponse(profile.getUserId(), profile.getDisplayName(), profile.getAvatarUrl())));

        return java.util.stream.IntStream.range(0, activities.size())
                .mapToObj(index -> {
                    FeedActivityProjection activity = activities.get(index);
                    FeedItemType itemType = activity.getItemRank() == 1
                            ? FeedItemType.REPOST
                            : FeedItemType.ORIGINAL;
                    PostAuthorResponse repostedBy = itemType == FeedItemType.REPOST
                            ? reposterResponses.get(activity.getActorId())
                            : null;
                    if (itemType == FeedItemType.REPOST && repostedBy == null) {
                        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
                    }
                    return new FeedItemResponse(itemType, activity.getActivityAt(), activity.getRepostedAt(),
                            repostedBy, postResponses.get(index));
                })
                .toList();
    }
}
