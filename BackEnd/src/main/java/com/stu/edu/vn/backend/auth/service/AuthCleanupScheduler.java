package com.stu.edu.vn.backend.auth.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Điều phối cleanup định kỳ; từng batch được transaction service khóa và commit độc lập. */
@Component
@ConditionalOnProperty(prefix = "auth.cleanup", name = "enabled", havingValue = "true")
public class AuthCleanupScheduler {
    private final AuthCleanupTransactionService cleanup;

    public AuthCleanupScheduler(AuthCleanupTransactionService cleanup) {
        this.cleanup = cleanup;
    }

    @Scheduled(fixedDelayString = "${auth.cleanup.fixed-delay:1h}")
    public void run() {
        drain(cleanup::expirePending);
        drain(cleanup::cleanupPending);
        drain(cleanup::expireSocial);
        drain(cleanup::cleanupSocial);
        drain(cleanup::expireLinkChallenges);
        drain(cleanup::cleanupLinkChallenges);
        drain(cleanup::expireReauthentication);
        drain(cleanup::cleanupReauthentication);
        drain(cleanup::expirePasswordRecovery);
        drain(cleanup::cleanupPasswordRecovery);
        drain(cleanup::cleanupExpiredRefreshTokens);
    }

    private void drain(BatchOperation operation) {
        // Lặp theo batch nhưng mỗi vòng là transaction riêng, tránh giữ khóa dài trên toàn bảng.
        while (operation.execute() > 0) {
            // Không có external call hoặc sleep trong vòng cleanup.
        }
    }

    @FunctionalInterface
    private interface BatchOperation {
        int execute();
    }
}
