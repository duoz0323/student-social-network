package com.stu.edu.vn.backend.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Khóa chính kép của bảng saved_posts, bảo đảm một người dùng chỉ lưu một bài viết tối đa một lần.
 */
@Embeddable
public class SavedPostId implements Serializable {

    @Column(name = "user_id", nullable = false)
    // userId luôn được lấy từ SecurityContext, không nhận từ dữ liệu do Client gửi lên.
    private Long userId;

    @Column(name = "post_id", nullable = false)
    // postId xác định bài viết được lưu và được lấy từ path variable của API.
    private Long postId;

    protected SavedPostId() {
        // Constructor rỗng dành cho JPA.
    }

    public SavedPostId(Long userId, Long postId) {
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
        if (!(object instanceof SavedPostId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }
}
