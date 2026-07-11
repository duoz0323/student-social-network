package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.post.dto.response.PostLikeResponse;

/**
 * Service nghiệp vụ Like/Unlike bài viết, luôn lấy người dùng hiện tại từ JWT/SecurityContext.
 */
public interface PostLikeService {

    PostLikeResponse likePost(Long postId);

    PostLikeResponse unlikePost(Long postId);
}
