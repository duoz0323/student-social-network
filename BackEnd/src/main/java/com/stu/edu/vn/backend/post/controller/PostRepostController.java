package com.stu.edu.vn.backend.post.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.post.dto.response.PostRepostResponse;
import com.stu.edu.vn.backend.post.service.PostRepostService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Nhận lệnh Repost/Unrepost idempotent và giao toàn bộ nghiệp vụ cho Service. */
@Validated
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostRepostController {
    private final PostRepostService postRepostService;

    @PutMapping("/{postId}/repost")
    public ResponseEntity<ApiResponse<PostRepostResponse>> repost(@PathVariable @Positive Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đăng lại bài viết thành công", postRepostService.repost(postId)));
    }

    @DeleteMapping("/{postId}/repost")
    public ResponseEntity<ApiResponse<PostRepostResponse>> unrepost(@PathVariable @Positive Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bỏ đăng lại bài viết thành công", postRepostService.unrepost(postId)));
    }
}
