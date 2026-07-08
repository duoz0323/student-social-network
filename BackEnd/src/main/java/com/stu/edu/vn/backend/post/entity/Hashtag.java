package com.stu.edu.vn.backend.post.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity hashtags lưu tên hashtag chuẩn hóa để dùng chung cho nhiều bài viết.
 */
@Entity
@Table(name = "hashtags")
public class Hashtag extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Khóa chính tự tăng của hashtag.
    private Long id;

    @Column(name = "normalized_name", nullable = false, length = 100, unique = true)
    // Tên hashtag đã chuẩn hóa chữ thường và bỏ ký tự # để chống trùng.
    private String normalizedName;

    @Column(name = "display_name", nullable = false, length = 100)
    // Tên hiển thị của hashtag, MVP có thể giống normalizedName.
    private String displayName;

    @Column(name = "post_count", nullable = false)
    // Bộ đếm số bài đang gắn hashtag, được database trigger cập nhật.
    private int postCount = 0;

    @OneToMany(mappedBy = "hashtag")
    // Danh sách quan hệ bài viết - hashtag, không cascade để tránh xóa dữ liệu dùng chung.
    private List<PostHashtag> postHashtags = new ArrayList<>();

    protected Hashtag() {
        // Constructor rỗng dành cho JPA.
    }

    public Hashtag(String normalizedName, String displayName) {
        // Constructor nghiệp vụ dùng sau khi hashtag đã được chuẩn hóa chữ thường.
        this.normalizedName = normalizedName;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public List<PostHashtag> getPostHashtags() {
        return postHashtags;
    }
}
