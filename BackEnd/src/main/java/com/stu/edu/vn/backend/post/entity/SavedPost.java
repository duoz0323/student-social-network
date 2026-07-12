package com.stu.edu.vn.backend.post.entity;

import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ quan hệ lưu bài giữa người dùng và bài viết trong bảng saved_posts.
 */
@Entity
@Table(name = "saved_posts")
public class SavedPost {

    @EmbeddedId
    // Khóa chính kép đồng thời là ràng buộc cuối cùng chống hai bản ghi Save trùng nhau.
    private SavedPostId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    // Người lưu bài được xác định từ JWT/SecurityContext tại tầng Service.
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    // Bài viết chỉ được tạo quan hệ Save sau khi Service xác nhận trạng thái PUBLISHED.
    private Post post;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    // Thời điểm lưu do MySQL tự sinh theo schema hiện có, Entity không tự ghi đè giá trị này.
    private LocalDateTime createdAt;

    protected SavedPost() {
        // Constructor rỗng dành cho JPA.
    }

    public SavedPost(User user, Post post) {
        this.user = user;
        this.post = post;
        this.id = new SavedPostId(user.getId(), post.getId());
    }

    public SavedPostId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Post getPost() {
        return post;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
