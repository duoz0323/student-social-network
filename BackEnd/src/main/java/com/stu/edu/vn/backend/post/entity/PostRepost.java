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

/** Quan hệ Repost chỉ tham chiếu bài gốc, không sao chép nội dung hoặc metadata của Post. */
@Entity
@Table(name = "post_reposts")
public class PostRepost {

    @EmbeddedId
    private PostRepostId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PostRepost() {
        // Constructor rỗng dành cho JPA.
    }

    public PostRepost(User user, Post post) {
        this.user = user;
        this.post = post;
        this.id = new PostRepostId(user.getId(), post.getId());
    }

    public PostRepostId getId() {
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
