package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.response.AdminPostDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminPostStatusResponse;
import com.stu.edu.vn.backend.admin.dto.request.AdminHidePostRequest;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.post.enums.PostStatus;

/** Use case đọc và thay đổi trạng thái kiểm duyệt bài viết dành cho ADMIN. */
public interface AdminPostService {
    PageResponse<AdminPostListItemResponse> getPosts(
            String keyword, PostStatus status, Long authorId, boolean reportedOnly, int page, int size);

    AdminPostDetailResponse getPostDetail(Long postId);

    AdminPostStatusResponse hidePost(Long postId, AdminHidePostRequest request);

    AdminPostStatusResponse restorePost(Long postId);
}
