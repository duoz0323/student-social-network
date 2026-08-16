package com.stu.edu.vn.backend.auth.entity;

import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.OtpDeliveryStatus;
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
 * Challenge OTP riêng để liên kết email vào user đang đăng nhập.
 */
@Entity
@Table(
        name = "auth_method_link_challenges",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_link_flow_token_hash", columnNames = "flow_token_hash"),
                @UniqueConstraint(name = "uq_link_active_identifier", columnNames = "active_identifier_key"),
                @UniqueConstraint(name = "uq_link_active_user_purpose", columnNames = "active_user_purpose_key")
        },
        indexes = {
                @Index(name = "idx_link_user_state", columnList = "user_id,status,expires_at,id"),
                @Index(name = "idx_link_expiry", columnList = "status,expires_at,id"),
                @Index(name = "idx_link_cleanup", columnList = "status,terminal_at,id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthMethodLinkChallenge extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 16)
    private AuthMethodLinkPurpose purpose;

    @Column(name = "identifier_normalized", nullable = true)
    private String identifierNormalized;

    @Column(name = "active_identifier_key", nullable = true, length = 272)
    private String activeIdentifierKey;

    @Column(name = "active_user_purpose_key", nullable = true, length = 64)
    private String activeUserPurposeKey;

    @Column(name = "flow_token_hash", nullable = true, length = 64, columnDefinition = "char(64)")
    private String flowTokenHash;

    @Column(name = "otp_hash", nullable = true, length = 64, columnDefinition = "char(64)")
    private String otpHash;

    @Column(name = "otp_verified_at", nullable = true)
    private LocalDateTime otpVerifiedAt;

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

    @Column(name = "terminal_at", nullable = true)
    private LocalDateTime terminalAt;

    public static AuthMethodLinkChallenge start(
            User user,
            AuthMethodLinkPurpose purpose,
            String identifierNormalized,
            String flowTokenHash,
            String otpHash,
            LocalDateTime otpExpiresAt,
            LocalDateTime resendAvailableAt,
            LocalDateTime expiresAt
    ) {
        Objects.requireNonNull(user, "user không được null");
        if (user.getId() == null) {
            throw new IllegalArgumentException("user phải được persist trước khi tạo link challenge");
        }
        AuthMethodLinkChallenge challenge = new AuthMethodLinkChallenge();
        challenge.user = user;
        challenge.purpose = Objects.requireNonNull(purpose, "purpose không được null");
        challenge.identifierNormalized = requireText(identifierNormalized, "identifierNormalized");
        challenge.activeIdentifierKey = purpose.name() + ":" + identifierNormalized;
        challenge.activeUserPurposeKey = user.getId() + ":" + purpose.name();
        challenge.flowTokenHash = requireText(flowTokenHash, "flowTokenHash");
        challenge.otpHash = requireText(otpHash, "otpHash");
        challenge.otpExpiresAt = Objects.requireNonNull(otpExpiresAt, "otpExpiresAt không được null");
        challenge.resendAvailableAt = Objects.requireNonNull(resendAvailableAt, "resendAvailableAt không được null");
        challenge.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt không được null");
        return challenge;
    }

    /** Phát hành OTP mới nhưng giữ nguyên TTL challenge. */
    public void resend(
            String newFlowTokenHash,
            String newOtpHash,
            LocalDateTime newOtpExpiresAt,
            LocalDateTime newResendAvailableAt
    ) {
        requirePending();
        this.flowTokenHash = requireText(newFlowTokenHash, "newFlowTokenHash");
        this.otpHash = requireText(newOtpHash, "newOtpHash");
        this.otpExpiresAt = Objects.requireNonNull(newOtpExpiresAt, "newOtpExpiresAt không được null");
        this.resendAvailableAt = Objects.requireNonNull(newResendAvailableAt, "newResendAvailableAt không được null");
        this.otpVersion++;
        this.failedAttempts = 0;
        this.resendCount++;
        this.deliveryStatus = OtpDeliveryStatus.PENDING;
        this.deliveryFailureCode = null;
    }

    public void complete(LocalDateTime completedAt) {
        transitionTo(OtpChallengeStatus.COMPLETED, completedAt);
    }

    /** Rotate flow token sau OTP để password step dùng proof riêng, ngắn hạn và single-use. */
    public void verifyOtp(String newFlowTokenHash, LocalDateTime verifiedAt) {
        requirePending();
        this.flowTokenHash = requireText(newFlowTokenHash, "newFlowTokenHash");
        this.otpVerifiedAt = Objects.requireNonNull(verifiedAt, "verifiedAt không được null");
    }

    public void cancel(LocalDateTime cancelledAt) {
        transitionTo(OtpChallengeStatus.CANCELLED, cancelledAt);
    }

    public void expire(LocalDateTime expiredAt) {
        transitionTo(OtpChallengeStatus.EXPIRED, expiredAt);
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

    private void transitionTo(OtpChallengeStatus terminalStatus, LocalDateTime occurredAt) {
        requirePending();
        this.status = terminalStatus;
        this.identifierNormalized = null;
        this.activeIdentifierKey = null;
        this.activeUserPurposeKey = null;
        this.flowTokenHash = null;
        this.otpHash = null;
        this.deliveryFailureCode = null;
        this.terminalAt = Objects.requireNonNull(occurredAt, "terminalAt không được null");
    }

    private void registerDeliveryAttempt(LocalDateTime attemptedAt) {
        this.lastDeliveryAttemptAt = Objects.requireNonNull(attemptedAt, "attemptedAt không được null");
        this.deliveryAttemptCount++;
    }

    private void requirePending() {
        if (status != OtpChallengeStatus.PENDING) {
            throw new IllegalStateException("Link challenge không còn ở trạng thái PENDING");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        return value;
    }
}
