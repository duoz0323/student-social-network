package com.stu.edu.vn.backend.messaging.service;

import com.stu.edu.vn.backend.messaging.dto.response.SharedPostAuthorResponse;
import com.stu.edu.vn.backend.messaging.dto.response.SharedPostResponse;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.mapper.PostMapper;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Hydrate PostCard theo từng viewer và batch toàn bộ trang lịch sử để tránh N+1. */
@Component
@RequiredArgsConstructor
public class SharedPostMessageLoader {
    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostMapper postMapper;

    public Map<Long, SharedPostResponse> loadVisible(Long viewerId, Collection<Long> sourcePostIds) {
        List<Long> postIds = sourcePostIds == null
                ? List.of()
                : new LinkedHashSet<>(sourcePostIds).stream().filter(java.util.Objects::nonNull).toList();
        if (postIds.isEmpty()) {
            return Map.of();
        }
        List<Post> posts = postRepository.findVisibleSharedPosts(viewerId, postIds, PostStatus.PUBLISHED);
        List<Long> authorIds = posts.stream().map(post -> post.getAuthor().getId()).distinct().toList();
        Map<Long, UserProfile> profiles = userProfileRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));
        Map<Long, List<PostMedia>> mediaByPost = new HashMap<>();
        for (PostMedia media : postMediaRepository.findByPost_IdInOrderByPost_IdAscDisplayOrderAsc(postIds)) {
            mediaByPost.computeIfAbsent(media.getPost().getId(), ignored -> new ArrayList<>()).add(media);
        }
        return posts.stream().collect(Collectors.toMap(Post::getId, post -> {
            UserProfile profile = profiles.get(post.getAuthor().getId());
            return new SharedPostResponse(post.getId(),
                    new SharedPostAuthorResponse(profile.getUserId(), profile.getUsername(),
                            profile.getDisplayName(), profile.getAvatarUrl()),
                    post.getContent(), mediaByPost.getOrDefault(post.getId(), List.of()).stream()
                    .map(postMapper::toMediaResponse).toList(),
                    post.getLikeCount(), post.getCommentCount(), post.getRepostCount());
        }));
    }
}
