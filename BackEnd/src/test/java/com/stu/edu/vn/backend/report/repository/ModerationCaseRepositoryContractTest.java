package com.stu.edu.vn.backend.report.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.LockModeType;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

class ModerationCaseRepositoryContractTest {

    @Test
    void adminDecisionUsesPessimisticWriteLock() throws Exception {
        Method method = ModerationCaseRepository.class.getMethod("findByIdForUpdate", Long.class);
        Lock lock = method.getAnnotation(Lock.class);
        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
