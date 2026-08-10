package com.stu.edu.vn.backend.report.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
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
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Vụ việc duy nhất của một trang cá nhân, gom toàn bộ lượt báo cáo từ nhiều người dùng. */
@Entity
@Table(name = "profile_report_cases")
public class ProfileReportCase extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    @Column(name = "reported_display_name_snapshot", nullable = false, length = 100)
    private String reportedDisplayNameSnapshot;

    @Column(name = "reported_avatar_url_snapshot", length = 1000)
    private String reportedAvatarUrlSnapshot;

    @Column(name = "reported_bio_snapshot", length = 500)
    private String reportedBioSnapshot;

    @Column(name = "reported_date_of_birth_snapshot")
    private LocalDate reportedDateOfBirthSnapshot;

    @Column(name = "report_count", nullable = false)
    private int reportCount;

    @Column(name = "latest_reported_at", nullable = false)
    private LocalDateTime latestReportedAt;

    protected ProfileReportCase() {
        // Constructor rỗng dành cho JPA.
    }

    public ProfileReportCase(User reportedUser, UserProfile profile, LocalDateTime reportedAt) {
        this.reportedUser = reportedUser;
        registerReport(profile, reportedAt);
    }

    /** Gắn lượt báo cáo mới và mở lại case nếu trước đó Admin đã kết luận. */
    public void registerReport(UserProfile profile, LocalDateTime reportedAt) {
        this.status = ReportStatus.PENDING;
        this.resolvedBy = null;
        this.resolvedAt = null;
        this.resolutionNote = null;
        this.reportedDisplayNameSnapshot = profile.getDisplayName();
        this.reportedAvatarUrlSnapshot = profile.getAvatarUrl();
        this.reportedBioSnapshot = profile.getBio();
        this.reportedDateOfBirthSnapshot = profile.getDateOfBirth();
        this.reportCount++;
        this.latestReportedAt = reportedAt;
    }

    public void resolve(User admin, LocalDateTime time, String note) {
        markProcessed(ReportStatus.RESOLVED, admin, time, note);
    }

    public void reject(User admin, LocalDateTime time, String note) {
        markProcessed(ReportStatus.REJECTED, admin, time, note);
    }

    private void markProcessed(ReportStatus target, User admin, LocalDateTime time, String note) {
        this.status = target;
        this.resolvedBy = admin;
        this.resolvedAt = time;
        this.resolutionNote = note;
    }

    public Long getId() { return id; }
    public User getReportedUser() { return reportedUser; }
    public ReportStatus getStatus() { return status; }
    public User getResolvedBy() { return resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public String getResolutionNote() { return resolutionNote; }
    public String getReportedDisplayNameSnapshot() { return reportedDisplayNameSnapshot; }
    public String getReportedAvatarUrlSnapshot() { return reportedAvatarUrlSnapshot; }
    public String getReportedBioSnapshot() { return reportedBioSnapshot; }
    public LocalDate getReportedDateOfBirthSnapshot() { return reportedDateOfBirthSnapshot; }
    public int getReportCount() { return reportCount; }
    public LocalDateTime getLatestReportedAt() { return latestReportedAt; }
}
