package com.stu.edu.vn.backend.auth.entity;

import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.enums.PasswordRecoveryStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Challenge thật hoặc decoy; chỉ lưu hash của OTP và token. */
@Entity
@Table(name = "password_recovery_challenges", indexes = {
        @Index(name = "idx_recovery_expiry", columnList = "status,challenge_expires_at,id"),
        @Index(name = "idx_recovery_cleanup", columnList = "status,updated_at,id"),
        @Index(name = "idx_recovery_user_status", columnList = "user_id,status,id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_recovery_flow_hash", columnNames = "recovery_flow_token_hash"),
        @UniqueConstraint(name = "uq_recovery_reset_hash", columnNames = "reset_token_hash"),
        @UniqueConstraint(name = "uq_recovery_active_subject", columnNames = "active_subject_key_hash")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordRecoveryChallenge extends BaseAuditEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @Column(name = "is_decoy", nullable = false) private boolean decoy;
    @Column(name = "subject_key_hash", nullable = false, length = 64, columnDefinition = "char(64)") private String subjectKeyHash;
    @Column(name = "active_subject_key_hash", length = 64, columnDefinition = "char(64)") private String activeSubjectKeyHash;
    @Enumerated(EnumType.STRING) @Column(name = "delivery_channel", nullable = false, length = 16) private RegistrationType deliveryChannel;
    @Column(name = "recovery_flow_token_hash", length = 64, columnDefinition = "char(64)") private String recoveryFlowTokenHash;
    @Column(name = "otp_hash", length = 64, columnDefinition = "char(64)") private String otpHash;
    @Column(name = "reset_token_hash", length = 64, columnDefinition = "char(64)") private String resetTokenHash;
    @Column(name = "otp_expires_at", nullable = false) private LocalDateTime otpExpiresAt;
    @Column(name = "challenge_expires_at", nullable = false) private LocalDateTime challengeExpiresAt;
    @Column(name = "reset_token_expires_at") private LocalDateTime resetTokenExpiresAt;
    @Column(name = "resend_available_at", nullable = false) private LocalDateTime resendAvailableAt;
    @Column(name = "failed_attempts", nullable = false) private int failedAttempts;
    @Column(name = "otp_version", nullable = false) private int otpVersion = 1;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 16) private PasswordRecoveryStatus status = PasswordRecoveryStatus.PENDING;
    @Enumerated(EnumType.STRING) @Column(name = "delivery_status", nullable = false, length = 16) private OtpDeliveryStatus deliveryStatus;
    @Column(name = "delivery_attempts", nullable = false) private int deliveryAttempts;
    @Column(name = "delivery_failure_code", length = 64) private String deliveryFailureCode;
    @Column(name = "verified_at") private LocalDateTime verifiedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;

    public static PasswordRecoveryChallenge start(User user, String subjectHash, RegistrationType channel,
            String flowHash, String otpHash, LocalDateTime otpExpiresAt, LocalDateTime resendAt,
            LocalDateTime challengeExpiresAt) {
        PasswordRecoveryChallenge value = new PasswordRecoveryChallenge();
        value.user = user;
        value.decoy = user == null;
        value.subjectKeyHash = subjectHash;
        value.activeSubjectKeyHash = subjectHash;
        value.deliveryChannel = channel;
        value.recoveryFlowTokenHash = flowHash;
        value.otpHash = otpHash;
        value.otpExpiresAt = otpExpiresAt;
        value.resendAvailableAt = resendAt;
        value.challengeExpiresAt = challengeExpiresAt;
        value.deliveryStatus = user == null ? OtpDeliveryStatus.NOT_APPLICABLE : OtpDeliveryStatus.PENDING;
        return value;
    }

    public void rotate(String flowHash, String otpHash, LocalDateTime otpExpiresAt, LocalDateTime resendAt) {
        require(PasswordRecoveryStatus.PENDING);
        this.recoveryFlowTokenHash = flowHash;
        this.otpHash = otpHash;
        this.otpExpiresAt = otpExpiresAt;
        this.resendAvailableAt = resendAt;
        this.failedAttempts = 0;
        this.otpVersion++;
        this.deliveryStatus = user == null ? OtpDeliveryStatus.NOT_APPLICABLE : OtpDeliveryStatus.PENDING;
        this.deliveryFailureCode = null;
    }

    public void rotateFlowToken(String flowHash) {
        require(PasswordRecoveryStatus.PENDING);
        this.recoveryFlowTokenHash = flowHash;
    }

    public void recordFailure(int maximum, LocalDateTime now) {
        require(PasswordRecoveryStatus.PENDING);
        failedAttempts++;
        if (failedAttempts >= maximum) terminate(PasswordRecoveryStatus.LOCKED, now);
    }

    public void verify(String resetHash, LocalDateTime resetExpiresAt, LocalDateTime now) {
        require(PasswordRecoveryStatus.PENDING);
        if (user == null) throw new IllegalStateException("Decoy không được phát hành reset token");
        status = PasswordRecoveryStatus.VERIFIED;
        recoveryFlowTokenHash = null;
        otpHash = null;
        activeSubjectKeyHash = null;
        resetTokenHash = resetHash;
        resetTokenExpiresAt = resetExpiresAt;
        verifiedAt = now;
    }

    public void complete(LocalDateTime now) {
        require(PasswordRecoveryStatus.VERIFIED);
        status = PasswordRecoveryStatus.COMPLETED;
        completedAt = now;
    }

    public void expire(LocalDateTime now) { terminate(PasswordRecoveryStatus.EXPIRED, now); }
    public void markDeliverySending() { require(PasswordRecoveryStatus.PENDING); deliveryStatus = OtpDeliveryStatus.SENDING; }
    public void markDeliverySent(int attempts) { deliveryAttempts += attempts; deliveryStatus = OtpDeliveryStatus.SENT; deliveryFailureCode = null; }
    public void markDeliveryFailed(String code, int attempts) { deliveryAttempts += attempts; deliveryStatus = OtpDeliveryStatus.FAILED; deliveryFailureCode = code; }
    public void markDeliveryUnknown(int attempts) { deliveryAttempts += attempts; deliveryStatus = OtpDeliveryStatus.UNKNOWN; deliveryFailureCode = null; }
    private void terminate(PasswordRecoveryStatus target, LocalDateTime now) {
        status = target; recoveryFlowTokenHash = null; otpHash = null; activeSubjectKeyHash = null;
        if (target == PasswordRecoveryStatus.EXPIRED && verifiedAt != null) resetTokenHash = null;
    }
    private void require(PasswordRecoveryStatus expected) {
        if (status != expected) throw new IllegalStateException("Password recovery challenge sai trạng thái");
    }
}
