package com.stu.edu.vn.backend.post.entity;

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
 * Entity post_hashtags biểu diễn hashtag tùy chọn của bài viết; hashtag có thể dùng chung cho nhiều bài.
 */
@Entity
@Table(name = "post_hashtags", uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        name = "uq_post_hashtags_post", columnNames = "post_id"))
public class PostHashtag {

    @EmbeddedId
    // Khóa kép gồm post_id và hashtag_id, không tạo id riêng ngoài schema.
    private PostHashtagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    // Bài viết sở hữu quan hệ hashtag, dùng LAZY để tránh tải bài khi chỉ cần hashtag.
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hashtagId")
    @JoinColumn(name = "hashtag_id", nullable = false)
    // Hashtag dùng chung, không cascade remove từ quan hệ này.
    private Hashtag hashtag;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    // Thời điểm tạo quan hệ do MySQL tự gán.
    private LocalDateTime createdAt;

    protected PostHashtag() {
        // Constructor rỗng dành cho JPA.
    }

    public PostHashtag(Post post, Hashtag hashtag) {
        // Khóa kép được lấy từ Post và Hashtag đã có id trong transaction tạo quan hệ.
        this.post = post;
        this.hashtag = hashtag;
        this.id = new PostHashtagId(
                post == null ? null : post.getId(),
                hashtag == null ? null : hashtag.getId()
        );
    }

    public PostHashtagId getId() {
        return id;
    }

    public void setId(PostHashtagId id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Hashtag getHashtag() {
        return hashtag;
    }

    public void setHashtag(Hashtag hashtag) {
        this.hashtag = hashtag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
