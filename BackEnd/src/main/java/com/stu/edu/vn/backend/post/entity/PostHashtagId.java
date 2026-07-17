package com.stu.edu.vn.backend.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Khóa chính kép của bảng post_hashtags; UNIQUE(post_id) mới bảo đảm mỗi bài có tối đa một hashtag.
 */
@Embeddable
public class PostHashtagId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "post_id")
    // Thành phần khóa chính trỏ tới bài viết.
    private Long postId;

    @Column(name = "hashtag_id")
    // Thành phần khóa chính trỏ tới hashtag dùng chung.
    private Long hashtagId;

    protected PostHashtagId() {
        // Constructor rỗng dành cho JPA.
    }

    public PostHashtagId(Long postId, Long hashtagId) {
        this.postId = postId;
        this.hashtagId = hashtagId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getHashtagId() {
        return hashtagId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PostHashtagId that)) {
            return false;
        }
        return Objects.equals(postId, that.postId)
                && Objects.equals(hashtagId, that.hashtagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, hashtagId);
    }
}
