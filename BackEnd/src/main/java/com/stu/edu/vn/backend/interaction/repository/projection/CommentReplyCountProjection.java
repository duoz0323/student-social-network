package com.stu.edu.vn.backend.interaction.repository.projection;

/**
 * Projection số reply đang hiển thị của từng bình luận gốc để tránh truy vấn N+1.
 */
public interface CommentReplyCountProjection {

    Long getCommentId();

    Long getReplyCount();
}
