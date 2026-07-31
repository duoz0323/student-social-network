package com.stu.edu.vn.backend.report.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.user.entity.User;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Hồ sơ kiểm duyệt gom các Report độc lập của cùng một bài viết trong một vòng xử lý.
 */
@Entity
@Table(name = "moderation_cases")
public class ModerationCase extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ModerationCaseStatus status;

    @Column(name = "report_count", nullable = false)
    private int reportCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    @Column(name = "first_reported_at", nullable = false)
    private LocalDateTime firstReportedAt;

    @Column(name = "latest_reported_at", nullable = false)
    private LocalDateTime latestReportedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "open_post_key", insertable = false, updatable = false)
    private Long openPostKey;

    protected ModerationCase() {
        // Constructor rỗng dành cho JPA.
    }

    public ModerationCase(Post post, LocalDateTime firstReportedAt) {
        this.post = post;
        this.status = ModerationCaseStatus.OPEN;
        this.reportCount = 0;
        this.firstReportedAt = firstReportedAt;
        this.latestReportedAt = firstReportedAt;
    }

    public void registerReport(LocalDateTime reportedAt) {
        // Bộ đếm và mốc gần nhất luôn thay đổi cùng thao tác tạo Report trong một transaction.
        this.reportCount += 1;
        this.latestReportedAt = reportedAt;
    }

    public void resolveNoViolation(User admin, String note, LocalDateTime resolvedAt) {
        resolve(ModerationCaseStatus.RESOLVED_NO_VIOLATION, admin, note, resolvedAt);
    }

    public void resolveActionTaken(User admin, String note, LocalDateTime resolvedAt) {
        resolve(ModerationCaseStatus.RESOLVED_ACTION_TAKEN, admin, note, resolvedAt);
    }

    private void resolve(ModerationCaseStatus target, User admin, String note, LocalDateTime time) {
        this.status = target;
        this.resolvedBy = admin;
        this.resolutionNote = note;
        this.resolvedAt = time;
    }

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public ModerationCaseStatus getStatus() { return status; }
    public int getReportCount() { return reportCount; }
    public User getResolvedBy() { return resolvedBy; }
    public String getResolutionNote() { return resolutionNote; }
    public LocalDateTime getFirstReportedAt() { return firstReportedAt; }
    public LocalDateTime getLatestReportedAt() { return latestReportedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
