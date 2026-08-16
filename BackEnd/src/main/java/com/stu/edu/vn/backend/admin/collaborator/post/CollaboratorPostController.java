package com.stu.edu.vn.backend.admin.collaborator.post;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.post.dto.request.*;
import com.stu.edu.vn.backend.post.dto.response.*;
import com.stu.edu.vn.backend.post.enums.LocationAction;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/collaborator")
@RequiredArgsConstructor
public class CollaboratorPostController {
    private final CollaboratorPostService service;

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_CREATE')")
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String hashtag,
            @RequestParam(required = false) List<MultipartFile> mediaFiles,
            @RequestPart(required = false) PostLocationRequest location) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo bài viết thành công",
                service.create(new CreatePostRequest(content, hashtag, mediaFiles, location))));
    }

    @GetMapping("/posts/{postId}")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_VIEW_OWN')")
    public ResponseEntity<ApiResponse<OwnedPostDetailResponse>> detail(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy bài viết thành công", service.detail(postId)));
    }

    @PutMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_UPDATE_OWN')")
    public ResponseEntity<ApiResponse<PostDetailResponse>> update(
            @PathVariable Long postId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String hashtag,
            @RequestParam(required = false) List<Long> keepMediaIds,
            @RequestParam(required = false) List<MultipartFile> newMediaFiles,
            @RequestParam(required = false) LocationAction locationAction,
            @RequestPart(required = false) PostLocationRequest location) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công", service.update(postId,
                new UpdatePostRequest(content, hashtag, keepMediaIds, newMediaFiles, locationAction, location))));
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_DELETE_OWN')")
    public ResponseEntity<ApiResponse<DeletePostResponse>> delete(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", service.delete(postId)));
    }

    @PutMapping("/posts/{postId}/like")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_LIKE')")
    public ResponseEntity<ApiResponse<PostLikeResponse>> like(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Like bài viết thành công", service.like(postId)));
    }

    @DeleteMapping("/posts/{postId}/like")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_LIKE')")
    public ResponseEntity<ApiResponse<PostLikeResponse>> unlike(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Unlike bài viết thành công", service.unlike(postId)));
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_COMMENT')")
    public ResponseEntity<ApiResponse<CommentResponse>> comment(@PathVariable Long postId,
            @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bình luận thành công", service.comment(postId, request)));
    }

    @PostMapping("/comments/{commentId}/replies")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_REPLY')")
    public ResponseEntity<ApiResponse<CommentResponse>> reply(@PathVariable Long commentId,
            @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trả lời bình luận thành công", service.reply(commentId, request)));
    }

    @PutMapping("/posts/{postId}/repost")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_REPOST')")
    public ResponseEntity<ApiResponse<PostRepostResponse>> repost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Repost thành công", service.repost(postId)));
    }

    @DeleteMapping("/posts/{postId}/repost")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_REPOST')")
    public ResponseEntity<ApiResponse<PostRepostResponse>> unrepost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Bỏ repost thành công", service.unrepost(postId)));
    }
}
