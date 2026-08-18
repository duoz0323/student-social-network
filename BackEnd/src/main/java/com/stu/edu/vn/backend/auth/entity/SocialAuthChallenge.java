package com.stu.edu.vn.backend.auth.entity;

import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
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
 * Challenge một lần dùng để xử lý social conflict mà không lưu raw provider token.
 */
@Entity
@Table(
        name = "social_auth_challenges",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_social_conflict_token_hash", columnNames = "conflict_token_hash"),
                @UniqueConstraint(name = "uq_social_active_provider", columnNames = "active_provider_key")
        },
        indexes = {
                @Index(name = "idx_social_pending_registration", columnList = "pending_registration_id,status,expires_at,id"),
                @Index(name = "idx_social_conflicting_user", columnList = "conflicting_user_id,status,id"),
                @Index(name = "idx_social_expiry", columnList = "status,expires_at,id"),
                @Index(name = "idx_social_cleanup", columnList = "status,terminal_at,id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAuthChallenge extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conflict_token_hash", nullable = true, length = 64, columnDefinition = "char(64)")
    private String conflictTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 16)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = true)
    private String providerUserId;

    @Column(name = "provider_identity_fingerprint", nullable = false, length = 64, columnDefinition = "char(64)")
    private String providerIdentityFingerprint;

    @Column(name = "provider_email", nullable = true)
    private String providerEmail;

    @Column(name = "provider_email_verified", nullable = true)
    private Boolean providerEmailVerified;

    @Column(name = "active_provider_key", nullable = true, length = 96)
    private String activeProviderKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_type", nullable = false, length = 64)
    private SocialConflictType conflictType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_registration_id", nullable = true)
    private PendingRegistration pendingRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conflicting_user_id", nullable = true)
    private User conflictingUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_action", nullable = true, length = 64)
    private SocialResolutionAction resolutionAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_user_id", nullable = true)
    private User resolvedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SocialAuthChallengeStatus status = SocialAuthChallengeStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "terminal_at", nullable = true)
    private LocalDateTime terminalAt;

    public static SocialAuthChallenge start(
            String conflictTokenHash,
            AuthProvider provider,
            String providerUserId,
            String providerIdentityFingerprint,
            String providerEmail,
            Boolean providerEmailVerified,
            SocialConflictType conflictType,
            PendingRegistration pendingRegistration,
            User conflictingUser,
            LocalDateTime expiresAt
    ) {
        validateProviderEmail(providerEmail, providerEmailVerified);
        validateConflictContext(conflictType, pendingRegistration, conflictingUser);
        SocialAuthChallenge challenge = new SocialAuthChallenge();
        challenge.conflictTokenHash = requireText(conflictTokenHash, "conflictTokenHash");
        challenge.provider = Objects.requireNonNull(provider, "provider không được null");
        challenge.providerUserId = requireText(providerUserId, "providerUserId");
        challenge.providerIdentityFingerprint = requireText(
                providerIdentityFingerprint,
                "providerIdentityFingerprint"
        );
        challenge.providerEmail = providerEmail;
        challenge.providerEmailVerified = providerEmailVerified;
        challenge.activeProviderKey = provider.name() + ":" + providerIdentityFingerprint;
        challenge.conflictType = conflictType;
        challenge.pendingRegistration = pendingRegistration;
        challenge.conflictingUser = conflictingUser;
        challenge.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt không được null");
        return challenge;
    }

    /** Resolve theo đúng cặp conflict/action; resolved user mới không được là conflicting user cũ. */
    public void resolve(SocialResolutionAction action, User resolvedUser, LocalDateTime resolvedAt) {
        requirePending();
        validateResolution(action, resolvedUser);
        this.resolutionAction = action;
        this.resolvedUser = resolvedUser;
        this.status = SocialAuthChallengeStatus.RESOLVED;
        clearProviderSecrets();
        this.terminalAt = Objects.requireNonNull(resolvedAt, "resolvedAt không được null");
    }

    public void cancel(LocalDateTime cancelledAt) {
        transitionWithoutResolution(SocialAuthChallengeStatus.CANCELLED, cancelledAt);
    }

    public void expire(LocalDateTime expiredAt) {
        transitionWithoutResolution(SocialAuthChallengeStatus.EXPIRED, expiredAt);
    }

    private void transitionWithoutResolution(SocialAuthChallengeStatus terminalStatus, LocalDateTime occurredAt) {
        requirePending();
        this.status = terminalStatus;
        this.resolutionAction = null;
        this.resolvedUser = null;
        clearProviderSecrets();
        this.terminalAt = Objects.requireNonNull(occurredAt, "terminalAt không được null");
    }

    private void clearProviderSecrets() {
        this.conflictTokenHash = null;
        this.providerUserId = null;
        this.providerEmail = null;
        this.providerEmailVerified = null;
        this.activeProviderKey = null;
    }

    private void validateResolution(SocialResolutionAction action, User targetUser) {
        Objects.requireNonNull(action, "resolutionAction không được null");
        boolean pendingConflict = conflictType == SocialConflictType.PENDING_EMAIL_MISMATCH
                || conflictType == SocialConflictType.PENDING_EMAIL_MISMATCH;
        if (pendingConflict && action == SocialResolutionAction.CONTINUE_OTP && targetUser == null) {
            return;
        }
        if (pendingConflict
                && action == SocialResolutionAction.CANCEL_PENDING_AND_CONTINUE_SOCIAL
                && targetUser != null) {
            return;
        }
        if (conflictType == SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER
                && (action == SocialResolutionAction.LOGIN_EXISTING_ACCOUNT
                || action == SocialResolutionAction.START_ACCOUNT_RECOVERY)
                && targetUser == null) {
            return;
        }
        if (conflictType == SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER
                && provider == AuthProvider.FACEBOOK
                && action == SocialResolutionAction.CONTINUE_WITH_SEPARATE_ACCOUNT
                && targetUser != null
                && isDifferentUser(targetUser, conflictingUser)) {
            return;
        }
        throw new IllegalArgumentException("resolutionAction không phù hợp conflictType hoặc resolvedUser");
    }

    private static boolean isDifferentUser(User targetUser, User conflictingUser) {
        if (targetUser == conflictingUser) {
            return false;
        }
        return targetUser.getId() == null || conflictingUser == null || conflictingUser.getId() == null
                || !targetUser.getId().equals(conflictingUser.getId());
    }

    private static void validateConflictContext(
            SocialConflictType conflictType,
            PendingRegistration pendingRegistration,
            User conflictingUser
    ) {
        Objects.requireNonNull(conflictType, "conflictType không được null");
        boolean pendingConflict = conflictType == SocialConflictType.PENDING_EMAIL_MISMATCH
                || conflictType == SocialConflictType.PENDING_EMAIL_MISMATCH;
        if (pendingConflict && pendingRegistration == null) {
            throw new IllegalArgumentException("Pending conflict phải tham chiếu pending registration");
        }
        if (conflictType == SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER && conflictingUser == null) {
            throw new IllegalArgumentException("ACTIVE email conflict phải tham chiếu conflicting user");
        }
    }

    private void requirePending() {
        if (status != SocialAuthChallengeStatus.PENDING) {
            throw new IllegalStateException("Social challenge không còn ở trạng thái PENDING");
        }
    }

    private static void validateProviderEmail(String email, Boolean verified) {
        if ((email == null) != (verified == null)) {
            throw new IllegalArgumentException("providerEmail và providerEmailVerified phải cùng null hoặc cùng có giá trị");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        return value;
    }
}
