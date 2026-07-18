package com.stu.edu.vn.backend.post.repository.projection;

import com.stu.edu.vn.backend.post.enums.PostStatus;

/**
 * Projection tối thiểu để kiểm tra trạng thái bài và xác định người nhận thông báo tương tác.
 */
public interface PostInteractionTargetProjection {

    Long getPostId();

    Long getAuthorId();

    PostStatus getStatus();
}
