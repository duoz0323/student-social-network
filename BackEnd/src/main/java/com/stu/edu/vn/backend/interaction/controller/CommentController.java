package com.stu.edu.vn.backend.interaction.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.dto.response.DeleteCommentResponse;
import com.stu.edu.vn.backend.interaction.service.CommentService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller bình luận chỉ nhận request và ủy quyền toàn bộ nghiệp vụ cho CommentService.
 */
@RestController
@Validated
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

    @PostMapping("/api/v1/comments/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<CommentResponse>> createReply(
                @PathVariable Long parentCommentId,
            @RequestBody CreateCommentRequest request
    ) {
        // postId được suy ra từ bình luận cha để không thể tạo reply sai bài viết.
        CommentResponse response = commentService.createReply(parentCommentId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trả lời bình luận thành công", response));
    }

    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getPublishedComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validatePagination(page, size);
        // Chỉ phân trang bình luận gốc; reply được tải riêng theo từng hội thoại.
        PageResponse<CommentResponse> response = commentService.getPublishedComments(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bình luận thành công", response));
    }

    @GetMapping("/api/v1/comments/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getPublishedReplies(
            @PathVariable Long parentCommentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validatePagination(page, size);
        PageResponse<CommentResponse> response = commentService.getPublishedReplies(parentCommentId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trả lời thành công", response));
    }

    @DeleteMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<ApiResponse<DeleteCommentResponse>> deleteComment(@PathVariable Long commentId) {
        // Xóa mềm comment của chính tác giả; Service không tự cập nhật comment_count.
        DeleteCommentResponse response = commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bình luận thành công", response));
    }

    private void validatePagination(int page, int size) {
        // Bảo vệ cả trường hợp Controller được gọi trực tiếp ngoài method-validation proxy.
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
