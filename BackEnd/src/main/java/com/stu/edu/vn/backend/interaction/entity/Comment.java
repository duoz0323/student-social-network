package com.stu.edu.vn.backend.interaction.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.interaction.enums.CommentStatus;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity comments lưu bình luận của người dùng trên bài viết và hỗ trợ xóa mềm.
 */
@Entity
@Table(name = "comments")
public class Comment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Khóa chính tự tăng của bình luận.
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    // Bài viết chứa bình luận, chỉ cho phép tạo khi bài viết còn PUBLISHED.
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    // Tác giả bình luận lấy từ JWT/SecurityContext, không nhận userId từ request.
    private User author;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    // Hồ sơ công khai của tác giả để trả displayName và avatarUrl trong response comment.
    private UserProfile authorProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    // Giai đoạn MVP hiện tại chỉ tạo bình luận cấp 1 nên giá trị này luôn để null khi thêm mới.
    private Comment parentComment;

    @Column(name = "content", nullable = false, length = 1000)
    // Nội dung bình luận đã được trim và không được rỗng theo nghiệp vụ.
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    // Trạng thái xóa mềm, chỉ comment PUBLISHED mới hiển thị trong danh sách.
    private CommentStatus status = CommentStatus.PUBLISHED;

    @Column(name = "deleted_at")
    // Thời điểm xóa mềm, bắt buộc có giá trị khi status chuyển sang DELETED.
    private LocalDateTime deletedAt;

    protected Comment() {
        // Constructor rỗng dành cho JPA.
    }

    public Comment(Post post, User author, String content) {
        this.post = post;
        this.author = author;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public User getAuthor() {
        return author;
    }

    public UserProfile getAuthorProfile() {
        return authorProfile;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public String getContent() {
        return content;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
