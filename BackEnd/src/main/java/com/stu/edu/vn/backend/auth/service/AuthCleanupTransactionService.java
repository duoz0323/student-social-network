package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.AuthCleanupProperties;
import com.stu.edu.vn.backend.auth.entity.AuthMethodLinkChallenge;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.entity.RefreshToken;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import com.stu.edu.vn.backend.auth.repository.AuthMethodLinkChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.PasswordRecoveryChallengeRepository;
import com.stu.edu.vn.backend.auth.enums.PasswordRecoveryStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mỗi lời gọi xử lý đúng một batch có khóa để transaction cleanup luôn ngắn. */
@Service
public class AuthCleanupTransactionService {
    private final PendingRegistrationRepository pending;
    private final SocialAuthChallengeRepository social;
    private final AuthMethodLinkChallengeRepository link;
    private final ReauthenticationChallengeRepository reauthentication;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordRecoveryChallengeRepository passwordRecovery;
    private final AuthCleanupProperties properties;
    private final Clock clock;

    public AuthCleanupTransactionService(PendingRegistrationRepository pending,
            SocialAuthChallengeRepository social, AuthMethodLinkChallengeRepository link,
            ReauthenticationChallengeRepository reauthentication, RefreshTokenRepository refreshTokens,
            PasswordRecoveryChallengeRepository passwordRecovery,
            AuthCleanupProperties properties, Clock clock) {
        this.pending = pending;
        this.social = social;
        this.link = link;
        this.reauthentication = reauthentication;
        this.refreshTokens = refreshTokens;
        this.passwordRecovery = passwordRecovery;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public int expirePending() {
        LocalDateTime now = now();
        List<PendingRegistration> rows = pending.findExpiryBatchForUpdate(
                OtpChallengeStatus.PENDING, now, page());
        rows.forEach(row -> row.expire(now));
        pending.saveAll(rows);
        return rows.size();
    }

    @Transactional
    public int cleanupPending() {
        // COMPLETED giữ liên kết audit tới user; contract chưa quy định retention nên không xóa.
        List<PendingRegistration> rows = pending.findCleanupBatchForUpdate(
                List.of(OtpChallengeStatus.CANCELLED, OtpChallengeStatus.EXPIRED), cutoff(), page());
        pending.deleteAllInBatch(rows);
        return rows.size();
    }

    @Transactional
    public int expireSocial() {
        LocalDateTime now = now();
        List<SocialAuthChallenge> rows = social.findExpiryBatchForUpdate(
                SocialAuthChallengeStatus.PENDING, now, page());
        rows.forEach(row -> row.expire(now));
        social.saveAll(rows);
        return rows.size();
    }

    @Transactional
    public int cleanupSocial() {
        List<SocialAuthChallenge> rows = social.findCleanupBatchForUpdate(cutoff(), page());
        social.deleteAllInBatch(rows);
        return rows.size();
    }

    @Transactional
    public int expireLinkChallenges() {
        LocalDateTime now = now();
        List<AuthMethodLinkChallenge> rows = link.findExpiryBatchForUpdate(
                OtpChallengeStatus.PENDING, now, page());
        rows.forEach(row -> row.expire(now));
        link.saveAll(rows);
        return rows.size();
    }

    @Transactional
    public int cleanupLinkChallenges() {
        List<AuthMethodLinkChallenge> rows = link.findCleanupBatchForUpdate(cutoff(), page());
        link.deleteAllInBatch(rows);
        return rows.size();
    }

    @Transactional
    public int expireReauthentication() {
        LocalDateTime now = now();
        List<ReauthenticationChallenge> rows = reauthentication.findExpiryBatchForUpdate(
                ReauthenticationChallengeStatus.ACTIVE, now, page());
        rows.forEach(row -> row.expire(now));
        reauthentication.saveAll(rows);
        return rows.size();
    }

    @Transactional
    public int cleanupReauthentication() {
        List<ReauthenticationChallenge> rows = reauthentication.findCleanupBatchForUpdate(cutoff(), page());
        reauthentication.deleteAllInBatch(rows);
        return rows.size();
    }

    @Transactional
    public int cleanupExpiredRefreshTokens() {
        List<RefreshToken> rows = refreshTokens.findExpiredBatchForUpdate(now(), page());
        refreshTokens.deleteAllInBatch(rows);
        return rows.size();
    }

    @Transactional
    public int expirePasswordRecovery() {
        LocalDateTime now = now();
        var rows = passwordRecovery.findExpiryBatchForUpdate(
                List.of(PasswordRecoveryStatus.PENDING, PasswordRecoveryStatus.VERIFIED), now, page());
        rows.forEach(row -> row.expire(now));
        passwordRecovery.saveAll(rows);
        return rows.size();
    }

    @Transactional
    public int cleanupPasswordRecovery() {
        var rows = passwordRecovery.findCleanupBatchForUpdate(List.of(PasswordRecoveryStatus.COMPLETED,
                PasswordRecoveryStatus.EXPIRED, PasswordRecoveryStatus.LOCKED), cutoff(), page());
        passwordRecovery.deleteAllInBatch(rows);
        return rows.size();
    }

    private PageRequest page() {
        return PageRequest.of(0, Math.max(1, properties.getBatchSize()));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private LocalDateTime cutoff() {
        return now().minus(properties.getRetention());
    }
}
