package com.stu.edu.vn.backend.interaction.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.dto.response.DeleteCommentResponse;
import com.stu.edu.vn.backend.interaction.service.CommentService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller bình luận chỉ nhận request và ủy quyền toàn bộ nghiệp vụ cho CommentService.
 */
@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request
    ) {
        // Service lấy userId từ JWT/SecurityContext, Controller tuyệt đối không nhận userId từ request.
        CommentResponse response = commentService.createComment(postId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm bình luận thành công", response));
    }

    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getPublishedComments(@PathVariable Long postId) {
        // Chỉ trả comment PUBLISHED của bài viết PUBLISHED, sắp xếp tăng dần theo thời gian ở Repository.
        List<CommentResponse> response = commentService.getPublishedComments(postId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bình luận thành công", response));
    }

    @DeleteMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<ApiResponse<DeleteCommentResponse>> deleteComment(@PathVariable Long commentId) {
        // Xóa mềm comment của chính tác giả; Service không tự cập nhật comment_count.
        DeleteCommentResponse response = commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bình luận thành công", response));
    }
}
