package com.stu.edu.vn.backend.post.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.post.dto.request.CreatePostRequest;
import com.stu.edu.vn.backend.post.dto.request.UpdatePostRequest;
import com.stu.edu.vn.backend.post.dto.response.DeletePostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostDetailResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.service.PostService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller bài viết chỉ nhận request, gom dữ liệu multipart thành DTO và ủy quyền nghiệp vụ cho PostService.
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "hashtag", required = false) String hashtag,
            @RequestParam(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles
    ) {
        // Request không có authorId; Service lấy tác giả hiện tại từ SecurityContext.
        PostResponse response = postService.createPost(new CreatePostRequest(content, hashtag, mediaFiles));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bài viết thành công", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(@PathVariable Long postId) {
        // Controller chỉ nhận postId từ URL, còn kiểm tra đăng nhập và quyền xem nằm trong Service.
        PostDetailResponse response = postService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết bài viết thành công", response));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long postId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "hashtag", required = false) String hashtag,
            @RequestParam(value = "keepMediaIds", required = false) List<Long> keepMediaIds,
            @RequestParam(value = "newMediaFiles", required = false) List<MultipartFile> newMediaFiles
    ) {
        // Controller không tự kiểm tra quyền; mọi rule tác giả, trạng thái và 15 phút nằm trong Service.
        PostDetailResponse response = postService.updatePost(
                postId,
                new UpdatePostRequest(content, hashtag, keepMediaIds, newMediaFiles)
        );
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<DeletePostResponse>> deletePost(@PathVariable Long postId) {
        // Controller chỉ nhận postId từ URL; Service kiểm tra JWT, hồ sơ, tác giả và trạng thái bài viết.
        DeletePostResponse response = postService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", response));
    }
}
