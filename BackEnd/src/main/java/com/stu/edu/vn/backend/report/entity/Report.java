package com.stu.edu.vn.backend.report.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
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
 * Entity reports lưu báo cáo và bằng chứng snapshot tại đúng thời điểm người dùng gửi báo cáo.
 */
@Entity
@Table(name = "reports")
public class Report extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    // Người báo cáo được lấy từ SecurityContext, không nhận từ request.
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    // Báo cáo do USER tạo luôn bắt đầu ở PENDING; chỉ luồng Admin tương lai mới được xử lý trạng thái.
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    @Column(name = "post_content_snapshot", length = 500)
    private String postContentSnapshot;

    @Column(name = "post_media_snapshot", columnDefinition = "json")
    // JSON chỉ được tạo từ media đọc trực tiếp trong database, không nhận từ Client.
    private String postMediaSnapshot;

    @Column(name = "pending_report_key", insertable = false, updatable = false)
    private String pendingReportKey;

    protected Report() {
        // Constructor rỗng dành cho JPA.
    }

    public Report(
            User reporter,
            Post post,
            ReportReason reason,
            String description,
            String postContentSnapshot,
            String postMediaSnapshot
    ) {
        this.reporter = reporter;
        this.post = post;
        this.reason = reason;
        this.description = description;
        this.postContentSnapshot = postContentSnapshot;
        this.postMediaSnapshot = postMediaSnapshot;
        this.status = ReportStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public User getReporter() {
        return reporter;
    }

    public Post getPost() {
        return post;
    }

    public ReportReason getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void resolve(User admin, LocalDateTime resolvedAt, String resolutionNote) {
        // Gom các trường kết quả để entity không rơi vào trạng thái RESOLVED thiếu người hoặc thời điểm xử lý.
        markProcessed(ReportStatus.RESOLVED, admin, resolvedAt, resolutionNote);
    }

    public void reject(User admin, LocalDateTime resolvedAt, String resolutionNote) {
        // REJECTED vẫn lưu đầy đủ người xử lý và ghi chú theo cùng invariant của schema.
        markProcessed(ReportStatus.REJECTED, admin, resolvedAt, resolutionNote);
    }

    private void markProcessed(
            ReportStatus targetStatus,
            User admin,
            LocalDateTime resolvedAt,
            String resolutionNote
    ) {
        this.status = targetStatus;
        this.resolvedBy = admin;
        this.resolvedAt = resolvedAt;
        this.resolutionNote = resolutionNote;
    }

    public String getPostContentSnapshot() {
        return postContentSnapshot;
    }

    public String getPostMediaSnapshot() {
        return postMediaSnapshot;
    }
}
