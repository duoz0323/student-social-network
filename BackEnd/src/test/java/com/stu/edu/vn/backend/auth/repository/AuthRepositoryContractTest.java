package com.stu.edu.vn.backend.auth.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import jakarta.persistence.LockModeType;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;

/**
 * Kiểm tra contract khóa của Repository mà không thay thế integration test MySQL.
 */
class AuthRepositoryContractTest {

    @Test
    void pendingRepositoryUsesPessimisticWriteForCriticalReadsAndBatches() throws Exception {
        assertPessimisticWrite(PendingRegistrationRepository.class, "findByFlowTokenHashForUpdate", String.class);
        assertPessimisticWrite(PendingRegistrationRepository.class, "findByIdForUpdate", Long.class);
        assertPessimisticWrite(PendingRegistrationRepository.class, "findByActiveIdentifierKeyForUpdate", String.class);
        assertPessimisticWrite(
                PendingRegistrationRepository.class,
                "findExpiryBatchForUpdate",
                OtpChallengeStatus.class,
                LocalDateTime.class,
                Pageable.class
        );
        assertPessimisticWrite(
                PendingRegistrationRepository.class,
                "findCleanupBatchForUpdate",
                LocalDateTime.class,
                Pageable.class
        );
    }

    @Test
    void linkRepositoryUsesPessimisticWriteForCriticalReadsAndBatches() throws Exception {
        assertPessimisticWrite(AuthMethodLinkChallengeRepository.class, "findByFlowTokenHashForUpdate", String.class);
        assertPessimisticWrite(
                AuthMethodLinkChallengeRepository.class,
                "findByActiveIdentifierKeyForUpdate",
                String.class
        );
        assertPessimisticWrite(
                AuthMethodLinkChallengeRepository.class,
                "findByActiveUserPurposeKeyForUpdate",
                String.class
        );
        assertPessimisticWrite(
                AuthMethodLinkChallengeRepository.class,
                "findExpiryBatchForUpdate",
                OtpChallengeStatus.class,
                LocalDateTime.class,
                Pageable.class
        );
    }

    @Test
    void socialAndReauthenticationRepositoriesLockTokenAndActiveKeyReads() throws Exception {
        assertPessimisticWrite(SocialAuthChallengeRepository.class, "findByConflictTokenHashForUpdate", String.class);
        assertPessimisticWrite(SocialAuthChallengeRepository.class, "findByActiveProviderKeyForUpdate", String.class);
        assertPessimisticWrite(
                SocialAuthChallengeRepository.class,
                "findExpiryBatchForUpdate",
                SocialAuthChallengeStatus.class,
                LocalDateTime.class,
                Pageable.class
        );
        assertPessimisticWrite(ReauthenticationChallengeRepository.class, "findByTokenHashForUpdate", String.class);
        assertPessimisticWrite(
                ReauthenticationChallengeRepository.class,
                "findByActiveUserScopeKeyForUpdate",
                String.class
        );
        assertPessimisticWrite(
                ReauthenticationChallengeRepository.class,
                "findExpiryBatchForUpdate",
                ReauthenticationChallengeStatus.class,
                LocalDateTime.class,
                Pageable.class
        );
    }

    private void assertPessimisticWrite(
            Class<?> repositoryType,
            String methodName,
            Class<?>... parameterTypes
    ) throws Exception {
        Method method = repositoryType.getMethod(methodName, parameterTypes);
        Lock lock = method.getAnnotation(Lock.class);
        assertNotNull(lock, repositoryType.getSimpleName() + "." + methodName + " phải khai báo @Lock");
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }
}
