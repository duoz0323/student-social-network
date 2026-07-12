package com.stu.edu.vn.backend.post.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.post.dto.response.PostSaveResponse;
import com.stu.edu.vn.backend.post.service.SavedPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller Save/Unsave chỉ nhận postId và ủy quyền toàn bộ kiểm tra nghiệp vụ cho Service.
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/saves")
public class SavedPostController {

    private final SavedPostService savedPostService;

    public SavedPostController(SavedPostService savedPostService) {
        this.savedPostService = savedPostService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostSaveResponse>> savePost(@PathVariable Long postId) {
        // Service tự lấy userId từ JWT/SecurityContext; Controller không nhận userId từ Client.
        PostSaveResponse response = savedPostService.savePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Lưu bài viết thành công", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<PostSaveResponse>> unsavePost(@PathVariable Long postId) {
        // Unsave luôn trả trạng thái saved=false kể cả khi quan hệ Save không còn tồn tại.
        PostSaveResponse response = savedPostService.unsavePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Bỏ lưu bài viết thành công", response));
    }
}
