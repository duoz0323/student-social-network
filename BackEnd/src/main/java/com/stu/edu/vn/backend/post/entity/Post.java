package com.stu.edu.vn.backend.post.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity posts lưu nội dung bài viết và trạng thái xóa mềm/ẩn theo nghiệp vụ MVP.
 */
@Entity
@Table(name = "posts")
public class Post extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Khóa chính tự tăng của bảng posts.
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    // Tác giả của bài viết, luôn lấy từ JWT ở tầng Service thay vì nhận từ Client.
    private User author;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    // Hồ sơ công khai của tác giả, ánh xạ read-only để API chi tiết tránh N+1 khi cần tên và avatar.
    private UserProfile authorProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "location_id",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_posts_location")
    )
    // Địa điểm tùy chọn dùng chung giữa nhiều bài; không cascade để bảo toàn Location khi xóa Post.
    private Location location;

    @Column(name = "content", length = 500)
    // Nội dung văn bản của bài viết, giới hạn 500 ký tự theo nghiệp vụ MVP.
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    // Trạng thái xóa mềm/ẩn bài, lưu bằng chuỗi để khớp ENUM MySQL.
    private PostStatus status = PostStatus.PUBLISHED;

    @Column(name = "is_edited", nullable = false)
    // Cờ cho biết bài viết đã từng được tác giả chỉnh sửa nội dung hoặc hashtag.
    private boolean edited = false;

    @Column(name = "like_count", nullable = false)
    // Bộ đếm like dư thừa có kiểm soát để tối ưu Feed For You.
    private int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    // Bộ đếm bình luận hợp lệ để tối ưu truy vấn Feed.
    private int commentCount = 0;

    @Column(name = "repost_count", nullable = false)
    // Bộ đếm Repost do trigger MySQL cập nhật để PostCard không phải COUNT theo từng bài.
    private int repostCount = 0;

    @Column(name = "published_at", nullable = false, insertable = false, updatable = false)
    // Thời điểm xuất bản do MySQL gán mặc định khi tạo bài.
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hidden_by")
    // Admin ẩn bài viết; NULL khi bài chưa bị ẩn.
    private User hiddenBy;

    @Column(name = "hidden_at")
    // Thời điểm Admin ẩn bài, dùng cùng status HIDDEN để bảo toàn lịch sử kiểm duyệt.
    private LocalDateTime hiddenAt;

    @Column(name = "hidden_reason", length = 500)
    // Lý do ẩn bài do Admin nhập, giới hạn 500 ký tự theo schema.
    private String hiddenReason;

    @Column(name = "deleted_at")
    // Thời điểm tác giả xóa mềm bài viết, chỉ có giá trị khi status là DELETED.
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    // Danh sách ảnh thuộc bài viết, không dùng EAGER để tránh tải thừa ở truy vấn danh sách.
    private List<PostMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    // Danh sách quan hệ hashtag của bài, không cascade remove sang Hashtag dùng chung.
    private List<PostHashtag> postHashtags = new ArrayList<>();

    protected Post() {
        // Constructor rỗng dành cho JPA.
    }

    public Post(User author, String content) {
        // Constructor nghiệp vụ tối thiểu để tạo bài từ tác giả lấy qua JWT.
        this.author = author;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public UserProfile getAuthorProfile() {
        return authorProfile;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(int repostCount) {
        this.repostCount = repostCount;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public User getHiddenBy() {
        return hiddenBy;
    }

    public void setHiddenBy(User hiddenBy) {
        this.hiddenBy = hiddenBy;
    }

    public LocalDateTime getHiddenAt() {
        return hiddenAt;
    }

    public void setHiddenAt(LocalDateTime hiddenAt) {
        this.hiddenAt = hiddenAt;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    public void setHiddenReason(String hiddenReason) {
        this.hiddenReason = hiddenReason;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<PostMedia> getMedia() {
        return media;
    }

    public List<PostHashtag> getPostHashtags() {
        return postHashtags;
    }
}
