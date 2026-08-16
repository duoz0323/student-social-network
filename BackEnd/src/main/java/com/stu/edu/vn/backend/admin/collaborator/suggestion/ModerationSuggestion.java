package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Đề xuất chỉ là đầu vào tham khảo, không tự thay đổi trạng thái bài viết. */
@Entity
@Table(name = "moderation_suggestions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModerationSuggestion extends BaseAuditEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suggested_by_admin_id", nullable = false)
    private User suggestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 32)
    private ModerationSuggestionReason reason;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ModerationSuggestionStatus status = ModerationSuggestionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public ModerationSuggestion(Post post, User suggestedBy, ModerationSuggestionReason reason, String description) {
        this.post = post;
        this.suggestedBy = suggestedBy;
        this.reason = reason;
        this.description = description;
    }

    public void review(ModerationSuggestionStatus decision, User reviewer, LocalDateTime reviewedAt) {
        this.status = decision;
        this.reviewedBy = reviewer;
        this.reviewedAt = reviewedAt;
    }
}
