package com.stu.edu.vn.backend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class RefreshTokenRepositoryContractTest {

    @Test
    void revokeUsesSingleBulkUpdateForActiveUnrevokedTokensWithoutClearingContext() throws Exception {
        Method method = RefreshTokenRepository.class.getMethod(
                "revokeAllActiveByUserId", Long.class, LocalDateTime.class);
        Modifying modifying = method.getAnnotation(Modifying.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(int.class);
        assertThat(modifying.flushAutomatically()).isTrue();
        assertThat(modifying.clearAutomatically()).isFalse();
        assertThat(query.value())
                .contains("update RefreshToken token")
                .contains("token.user.id = :userId")
                .contains("token.revokedAt is null")
                .contains("token.expiresAt > :now")
                .doesNotContain("select");
    }
}
