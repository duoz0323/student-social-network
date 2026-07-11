package com.stu.edu.vn.backend.interaction.service;

import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.dto.response.DeleteCommentResponse;
import java.util.List;

/**
 * Service nghiệp vụ bình luận bài viết, luôn lấy người dùng hiện tại từ JWT/SecurityContext.
 */
public interface CommentService {

    CommentResponse createComment(Long postId, CreateCommentRequest request);

    List<CommentResponse> getPublishedComments(Long postId);

    DeleteCommentResponse deleteComment(Long commentId);
}
