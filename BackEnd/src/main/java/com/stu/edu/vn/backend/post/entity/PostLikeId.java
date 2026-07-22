package com.stu.edu.vn.backend.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Khóa chính kép của bảng post_likes, bảo đảm một người dùng chỉ Like một bài viết tối đa một lần.
 */
@Embeddable
public class PostLikeId implements Serializable {

    @Column(name = "user_id", nullable = false)
    // userId lấy từ JWT/current authenticated user, tuyệt đối không nhận từ request Like/Unlike.
    private Long userId;

    @Column(name = "post_id", nullable = false)
    // postId lấy từ path variable để xác định bài viết đang được tương tác.
    private Long postId;

    protected PostLikeId() {
        // Constructor rỗng dành cho JPA.
    }

    public PostLikeId(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPostId() {
        return postId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PostLikeId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }
}
