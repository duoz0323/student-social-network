package com.stu.edu.vn.backend.auth.entity;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
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
 * Bằng chứng xác thực lại có thời hạn ngắn trước thao tác bảo mật nhạy cảm.
 */
@Entity
@Table(
        name = "reauthentication_challenges",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_reauth_token_hash", columnNames = "token_hash"),
                @UniqueConstraint(name = "uq_reauth_active_user_scope", columnNames = "active_user_scope_key")
        },
        indexes = {
                @Index(name = "idx_reauth_user_state", columnList = "user_id,status,expires_at,id"),
                @Index(name = "idx_reauth_expiry", columnList = "status,expires_at,id"),
                @Index(name = "idx_reauth_cleanup", columnList = "status,terminal_at,id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReauthenticationChallenge extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = true, length = 64, columnDefinition = "char(64)")
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "proof_method", nullable = false, length = 32)
    private ReauthenticationProofMethod proofMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 32)
    private ReauthenticationScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_auth_method", nullable = false, length = 16)
    private AuthMethod targetAuthMethod;

    @Column(name = "active_user_scope_key", nullable = true, length = 96)
    private String activeUserScopeKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReauthenticationChallengeStatus status = ReauthenticationChallengeStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "terminal_at", nullable = true)
    private LocalDateTime terminalAt;

    public static ReauthenticationChallenge start(
            User user,
            String tokenHash,
            ReauthenticationProofMethod proofMethod,
            ReauthenticationScope scope,
            AuthMethod targetAuthMethod,
            LocalDateTime expiresAt
    ) {
        Objects.requireNonNull(user, "user không được null");
        if (user.getId() == null) {
            throw new IllegalArgumentException("user phải được persist trước khi tạo reauthentication challenge");
        }
        ReauthenticationChallenge challenge = new ReauthenticationChallenge();
        challenge.user = user;
        challenge.tokenHash = requireText(tokenHash, "tokenHash");
        challenge.proofMethod = Objects.requireNonNull(proofMethod, "proofMethod không được null");
        challenge.scope = Objects.requireNonNull(scope, "scope không được null");
        challenge.targetAuthMethod = Objects.requireNonNull(targetAuthMethod, "targetAuthMethod không được null");
        challenge.activeUserScopeKey = user.getId() + ":" + scope.name();
        challenge.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt không được null");
        return challenge;
    }

    public void consume(LocalDateTime consumedAt) {
        transitionTo(ReauthenticationChallengeStatus.CONSUMED, consumedAt);
    }

    public void cancel(LocalDateTime cancelledAt) {
        transitionTo(ReauthenticationChallengeStatus.CANCELLED, cancelledAt);
    }

    public void expire(LocalDateTime expiredAt) {
        transitionTo(ReauthenticationChallengeStatus.EXPIRED, expiredAt);
    }

    private void transitionTo(ReauthenticationChallengeStatus terminalStatus, LocalDateTime occurredAt) {
        requireActive();
        this.status = terminalStatus;
        this.tokenHash = null;
        this.activeUserScopeKey = null;
        this.terminalAt = Objects.requireNonNull(occurredAt, "terminalAt không được null");
    }

    private void requireActive() {
        if (status != ReauthenticationChallengeStatus.ACTIVE) {
            throw new IllegalStateException("Reauthentication challenge không còn ACTIVE");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        return value;
    }
}
