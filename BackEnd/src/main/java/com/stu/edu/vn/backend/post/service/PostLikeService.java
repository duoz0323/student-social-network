package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostLikeResponse;


/**
 * Service nghiệp vụ Like/Unlike bài viết, luôn lấy người dùng hiện tại từ JWT/SecurityContext.
 */
public interface PostLikeService {

    PostLikeResponse likePost(Long postId);
    PostLikeResponse likePostAs(Long userId, Long postId);

    PostLikeResponse unlikePost(Long postId);
    PostLikeResponse unlikePostAs(Long userId, Long postId);

    CursorPageResponse<FeedPostResponse> getLikedPosts(String cursor, int limit);
}
