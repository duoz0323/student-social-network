package com.stu.edu.vn.backend.report.entity;

import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.report.enums.ProfileReportReason;
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

/** Báo cáo hồ sơ độc lập, giữ snapshot để Admin còn bằng chứng khi hồ sơ đã thay đổi. */
@Entity
@Table(name = "profile_reports")
public class ProfileReport extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private ProfileReportCase reportCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ProfileReportReason reason;

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

    @Column(name = "reporter_display_name_snapshot", nullable = false, length = 100)
    private String reporterDisplayNameSnapshot;

    @Column(name = "reported_display_name_snapshot", nullable = false, length = 100)
    private String reportedDisplayNameSnapshot;

    @Column(name = "reported_avatar_url_snapshot", length = 1000)
    private String reportedAvatarUrlSnapshot;

    @Column(name = "reported_bio_snapshot", length = 500)
    private String reportedBioSnapshot;

    @Column(name = "reported_date_of_birth_snapshot")
    private LocalDate reportedDateOfBirthSnapshot;

    @Column(name = "pending_report_key", insertable = false, updatable = false)
    private String pendingReportKey;

    protected ProfileReport() {
        // Constructor rỗng dành cho JPA.
    }

    public ProfileReport(
            ProfileReportCase reportCase,
            User reporter,
            User reportedUser,
            ProfileReportReason reason,
            UserProfile reporterProfile,
            UserProfile reportedProfile
    ) {
        this.reportCase = reportCase;
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.reason = reason;
        this.reporterDisplayNameSnapshot = reporterProfile.getDisplayName();
        this.reportedDisplayNameSnapshot = reportedProfile.getDisplayName();
        this.reportedAvatarUrlSnapshot = reportedProfile.getAvatarUrl();
        this.reportedBioSnapshot = reportedProfile.getBio();
        this.reportedDateOfBirthSnapshot = reportedProfile.getDateOfBirth();
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
    public ProfileReportCase getReportCase() { return reportCase; }
    public User getReporter() { return reporter; }
    public User getReportedUser() { return reportedUser; }
    public ProfileReportReason getReason() { return reason; }
    public ReportStatus getStatus() { return status; }
    public User getResolvedBy() { return resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public String getResolutionNote() { return resolutionNote; }
    public String getReporterDisplayNameSnapshot() { return reporterDisplayNameSnapshot; }
    public String getReportedDisplayNameSnapshot() { return reportedDisplayNameSnapshot; }
    public String getReportedAvatarUrlSnapshot() { return reportedAvatarUrlSnapshot; }
    public String getReportedBioSnapshot() { return reportedBioSnapshot; }
    public LocalDate getReportedDateOfBirthSnapshot() { return reportedDateOfBirthSnapshot; }
}
