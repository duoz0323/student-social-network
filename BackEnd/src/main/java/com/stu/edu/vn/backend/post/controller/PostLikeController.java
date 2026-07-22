package com.stu.edu.vn.backend.post.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.post.dto.response.PostLikeResponse;
import com.stu.edu.vn.backend.post.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller Like/Unlike bài viết, chỉ nhận postId từ URL và không nhận userId từ request.
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostLikeResponse>> likePost(@PathVariable Long postId) {
        // Service lấy userId hiện tại từ JWT/SecurityContext và kiểm tra trạng thái bài viết trước khi Like.
        PostLikeResponse response = postLikeService.likePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Like bài viết thành công", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<PostLikeResponse>> unlikePost(@PathVariable Long postId) {
        // Service xóa post_likes và đọc lại like_count do trigger database cập nhật.
        PostLikeResponse response = postLikeService.unlikePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Bỏ Like bài viết thành công", response));
    }
}
