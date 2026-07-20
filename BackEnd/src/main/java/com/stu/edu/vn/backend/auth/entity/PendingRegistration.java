package com.stu.edu.vn.backend.auth.entity;

import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.common.entity.BaseAuditEntity;
import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Đăng ký local đang chờ OTP; chưa đại diện cho một tài khoản users thực sự.
 */
@Entity
@Table(
        name = "pending_registrations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_pending_flow_token_hash", columnNames = "flow_token_hash"),
                @UniqueConstraint(name = "uq_pending_active_identifier", columnNames = "active_identifier_key"),
                @UniqueConstraint(name = "uq_pending_completed_user", columnNames = "completed_user_id")
        },
        indexes = {
                @Index(name = "idx_pending_identifier_state", columnList = "identifier_normalized,status,expires_at,id"),
                @Index(name = "idx_pending_expiry", columnList = "status,expires_at,id"),
                @Index(name = "idx_pending_cleanup", columnList = "status,terminal_at,id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PendingRegistration extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false, length = 16)
    private RegistrationType registrationType;

    @Column(name = "identifier_normalized", nullable = true)
    private String identifierNormalized;

    @Column(name = "active_identifier_key", nullable = true, length = 272)
    private String activeIdentifierKey;

    @Column(name = "password_hash", nullable = true)
    private String passwordHash;

    @Column(name = "flow_token_hash", nullable = true, length = 64, columnDefinition = "char(64)")
    private String flowTokenHash;

    @Column(name = "otp_hash", nullable = true, length = 64, columnDefinition = "char(64)")
    private String otpHash;

    @Column(name = "otp_version", nullable = false, columnDefinition = "int unsigned")
    private Integer otpVersion = 1;

    @Column(name = "otp_expires_at", nullable = false)
    private LocalDateTime otpExpiresAt;

    @Column(name = "failed_attempts", nullable = false, columnDefinition = "tinyint unsigned")
    private Integer failedAttempts = 0;

    @Column(name = "resend_available_at", nullable = false)
    private LocalDateTime resendAvailableAt;

    @Column(name = "resend_count", nullable = false, columnDefinition = "smallint unsigned")
    private Integer resendCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 16)
    private OtpDeliveryStatus deliveryStatus = OtpDeliveryStatus.PENDING;

    @Column(name = "delivery_attempt_count", nullable = false, columnDefinition = "smallint unsigned")
    private Integer deliveryAttemptCount = 0;

    @Column(name = "last_delivery_attempt_at", nullable = true)
    private LocalDateTime lastDeliveryAttemptAt;

    @Column(name = "last_delivery_succeeded_at", nullable = true)
    private LocalDateTime lastDeliverySucceededAt;

    @Column(name = "delivery_failure_code", nullable = true, length = 64)
    private String deliveryFailureCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OtpChallengeStatus status = OtpChallengeStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_user_id", nullable = true)
    private User completedUser;

    @Column(name = "terminal_at", nullable = true)
    private LocalDateTime terminalAt;

    public static PendingRegistration start(
            RegistrationType registrationType,
            String identifierNormalized,
            String passwordHash,
            String flowTokenHash,
            String otpHash,
            LocalDateTime otpExpiresAt,
            LocalDateTime resendAvailableAt,
            LocalDateTime expiresAt
    ) {
        PendingRegistration registration = new PendingRegistration();
        registration.registrationType = Objects.requireNonNull(registrationType, "registrationType không được null");
        registration.identifierNormalized = requireText(identifierNormalized, "identifierNormalized");
        registration.activeIdentifierKey = registrationType.name() + ":" + identifierNormalized;
        registration.passwordHash = requireText(passwordHash, "passwordHash");
        registration.flowTokenHash = requireText(flowTokenHash, "flowTokenHash");
        registration.otpHash = requireText(otpHash, "otpHash");
        registration.otpExpiresAt = Objects.requireNonNull(otpExpiresAt, "otpExpiresAt không được null");
        registration.resendAvailableAt = Objects.requireNonNull(resendAvailableAt, "resendAvailableAt không được null");
        registration.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt không được null");
        return registration;
    }

    /** Phát hành OTP mới nhưng giữ nguyên flow token, password hash và thời hạn pending. */
    public void resend(
            String newOtpHash,
            LocalDateTime newOtpExpiresAt,
            LocalDateTime newResendAvailableAt
    ) {
        requirePending();
        this.otpHash = requireText(newOtpHash, "newOtpHash");
        this.otpExpiresAt = Objects.requireNonNull(newOtpExpiresAt, "newOtpExpiresAt không được null");
        this.resendAvailableAt = Objects.requireNonNull(newResendAvailableAt, "newResendAvailableAt không được null");
        this.otpVersion++;
        this.failedAttempts = 0;
        this.resendCount++;
        this.deliveryStatus = OtpDeliveryStatus.PENDING;
        this.deliveryFailureCode = null;
    }

    /** Hoàn tất pending, xóa verification secret nhưng giữ HMAC lookup hash trong thời hạn retention. */
    public void complete(User user, LocalDateTime completedAt) {
        requirePending();
        this.completedUser = Objects.requireNonNull(user, "completedUser không được null");
        this.status = OtpChallengeStatus.COMPLETED;
        this.activeIdentifierKey = null;
        clearVerificationSecrets();
        this.terminalAt = Objects.requireNonNull(completedAt, "completedAt không được null");
    }

    public void cancel(LocalDateTime cancelledAt) {
        transitionWithoutAccount(OtpChallengeStatus.CANCELLED, cancelledAt);
    }

    public void expire(LocalDateTime expiredAt) {
        transitionWithoutAccount(OtpChallengeStatus.EXPIRED, expiredAt);
    }

    public void recordFailedAttempt() {
        requirePending();
        this.failedAttempts++;
    }

    public void markDeliverySent(LocalDateTime attemptedAt) {
        requirePending();
        registerDeliveryAttempt(attemptedAt);
        this.deliveryStatus = OtpDeliveryStatus.SENT;
        this.lastDeliverySucceededAt = attemptedAt;
        this.deliveryFailureCode = null;
    }

    public void markDeliveryFailed(LocalDateTime attemptedAt, String failureCode) {
        requirePending();
        registerDeliveryAttempt(attemptedAt);
        this.deliveryStatus = OtpDeliveryStatus.FAILED;
        this.deliveryFailureCode = requireText(failureCode, "failureCode");
    }

    public void markDeliveryUnknown(LocalDateTime attemptedAt) {
        requirePending();
        registerDeliveryAttempt(attemptedAt);
        this.deliveryStatus = OtpDeliveryStatus.UNKNOWN;
        this.deliveryFailureCode = null;
    }

    private void transitionWithoutAccount(OtpChallengeStatus terminalStatus, LocalDateTime occurredAt) {
        requirePending();
        this.status = terminalStatus;
        this.identifierNormalized = null;
        this.activeIdentifierKey = null;
        this.completedUser = null;
        clearVerificationSecrets();
        this.terminalAt = Objects.requireNonNull(occurredAt, "terminalAt không được null");
    }

    private void clearVerificationSecrets() {
        this.passwordHash = null;
        this.otpHash = null;
        this.deliveryFailureCode = null;
    }

    private void registerDeliveryAttempt(LocalDateTime attemptedAt) {
        this.lastDeliveryAttemptAt = Objects.requireNonNull(attemptedAt, "attemptedAt không được null");
        this.deliveryAttemptCount++;
    }

    private void requirePending() {
        if (status != OtpChallengeStatus.PENDING) {
            throw new IllegalStateException("Pending registration không còn ở trạng thái PENDING");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        return value;
    }
}
