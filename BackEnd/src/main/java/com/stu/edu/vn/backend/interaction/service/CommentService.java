package com.stu.edu.vn.backend.interaction.service;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.dto.response.DeleteCommentResponse;

/**
 * Service nghiệp vụ bình luận bài viết, luôn lấy người dùng hiện tại từ JWT/SecurityContext.
 */
public interface CommentService {

    CommentResponse createComment(Long postId, CreateCommentRequest request);
    CommentResponse createCommentAs(Long userId, Long postId, CreateCommentRequest request);

    CommentResponse createReply(Long parentCommentId, CreateCommentRequest request);
    CommentResponse createReplyAs(Long userId, Long parentCommentId, CreateCommentRequest request);

    PageResponse<CommentResponse> getPublishedComments(Long postId, int page, int size);
    PageResponse<CommentResponse> getPublishedCommentsAs(Long userId, Long postId, int page, int size);

    PageResponse<CommentResponse> getPublishedReplies(Long parentCommentId, int page, int size);
    PageResponse<CommentResponse> getPublishedRepliesAs(Long userId, Long parentCommentId, int page, int size);

    DeleteCommentResponse deleteComment(Long commentId);
}
