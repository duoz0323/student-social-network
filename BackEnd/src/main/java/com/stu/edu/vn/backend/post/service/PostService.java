package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.post.dto.request.CreatePostRequest;
import com.stu.edu.vn.backend.post.dto.request.UpdatePostRequest;
import com.stu.edu.vn.backend.post.dto.response.DeletePostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostDetailResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;

/**
 * Service nghiệp vụ bài viết, giai đoạn này chỉ triển khai tạo bài.
 */
public interface PostService {

    PostResponse createPost(CreatePostRequest request);

    PostDetailResponse getPostDetail(Long postId);

    PostDetailResponse updatePost(Long postId, UpdatePostRequest request);

    DeletePostResponse deletePost(Long postId);
}
