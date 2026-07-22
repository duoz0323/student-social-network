package com.stu.edu.vn.backend.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class RefreshTokenRepositoryContractTest {

    @Test
    void revokeUsesSingleBulkUpdateForOnlyUnrevokedUnexpiredTokens() throws Exception {
        Method method = RefreshTokenRepository.class.getMethod(
                "revokeActiveTokensByUserId", Long.class, LocalDateTime.class);
        Modifying modifying = method.getAnnotation(Modifying.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getReturnType()).isEqualTo(int.class);
        assertThat(modifying.flushAutomatically()).isTrue();
        assertThat(query.value())
                .contains("update RefreshToken token")
                .contains("token.user.id = :userId")
                .contains("token.revokedAt is null")
                .contains("token.expiresAt > :revokedAt")
                .doesNotContain("select");
    }
}
