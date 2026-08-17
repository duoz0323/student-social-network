package com.stu.edu.vn.backend.feed.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.mapper.FeedPostMapper;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepostRepository;
import com.stu.edu.vn.backend.post.repository.SavedPostRepository;
import com.stu.edu.vn.backend.post.service.PostLocationBatchLoader;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.service.PublicUserBadgeService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Batch-load toàn bộ dữ liệu cần để render PostCard, không truy vấn trong vòng lặp từng bài. */
@Component
@RequiredArgsConstructor
public class FeedPostBatchLoader {
    private final UserProfileRepository userProfileRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;
    private final PostRepostRepository postRepostRepository;
    private final FeedPostMapper feedPostMapper;
    private final PostLocationBatchLoader postLocationBatchLoader;
    private final PublicUserBadgeService publicUserBadgeService;

    public List<FeedPostResponse> map(List<Post> posts, Long viewerId) {
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> authorIds = posts.stream().map(post -> post.getAuthor().getId()).distinct().toList();
        Map<Long, UserProfile> profiles = userProfileRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        var badges = publicUserBadgeService.getBadgesByUserIds(authorIds);
        Map<Long, List<PostMedia>> media = new HashMap<>();
        for (PostMedia item : postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(postIds)) {
            media.computeIfAbsent(item.getPost().getId(), ignored -> new ArrayList<>()).add(item);
        }
        Map<Long, String> hashtags = new HashMap<>();
        for (PostHashtag relation : postHashtagRepository.findWithHashtagByPostIds(postIds)) {
            if (hashtags.put(relation.getPost().getId(), relation.getHashtag().getNormalizedName()) != null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
        }
        Set<Long> liked = new HashSet<>(postLikeRepository.findLikedPostIds(viewerId, postIds));
        Set<Long> saved = new HashSet<>(savedPostRepository.findSavedPostIds(viewerId, postIds));
        Set<Long> reposted = new HashSet<>(postRepostRepository.findRepostedPostIds(viewerId, postIds));
        Map<Long, Location> locations = postLocationBatchLoader.loadByPostId(posts);
        return posts.stream().map(post -> feedPostMapper.toResponse(
                post, profiles.get(post.getAuthor().getId()),
                media.getOrDefault(post.getId(), List.of()), hashtags.get(post.getId()),
                liked.contains(post.getId()), saved.contains(post.getId()), reposted.contains(post.getId()),
                locations.get(post.getId()),
                badges.getOrDefault(post.getAuthor().getId(), List.of())
        )).toList();
    }
}
