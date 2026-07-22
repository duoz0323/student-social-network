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
 * Entity ánh xạ bảng post_likes, nơi lưu quan hệ Like giữa người dùng và bài viết.
 */
@Entity
@Table(name = "post_likes")
public class PostLike {

    @EmbeddedId
    // Khóa chính kép đồng thời là ràng buộc chống Like trùng ở tầng database.
    private PostLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    // Người dùng thực hiện Like, được xác định từ SecurityContext thay vì dữ liệu Client gửi lên.
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    // Bài viết được Like, chỉ được phép ở trạng thái PUBLISHED theo nghiệp vụ MVP.
    private Post post;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    // Thời điểm Like do MySQL tự sinh để đồng nhất thời gian lưu trữ.
    private LocalDateTime createdAt;

    protected PostLike() {
        // Constructor rỗng dành cho JPA.
    }

    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
        this.id = new PostLikeId(user.getId(), post.getId());
    }

    public PostLikeId getId() {
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
