package com.stu.edu.vn.backend.location.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class LocationRepositoryContractTest {

    @Test
    void repositoryExposesNaturalKeyLookups() throws Exception {
        // Repository chỉ cung cấp các truy vấn cơ bản theo Google Place ID trong Giai đoạn 2.
        assertThat(JpaRepository.class).isAssignableFrom(LocationRepository.class);
        Method findMethod = LocationRepository.class.getMethod("findByGooglePlaceId", String.class);
        Method existsMethod = LocationRepository.class.getMethod("existsByGooglePlaceId", String.class);
        Method insertMethod = LocationRepository.class.getMethod("insertIfAbsent", String.class, String.class,
                String.class, java.math.BigDecimal.class, java.math.BigDecimal.class);

        assertThat(findMethod.getReturnType()).isEqualTo(java.util.Optional.class);
        assertThat(existsMethod.getReturnType()).isEqualTo(boolean.class);
        assertThat(insertMethod.getReturnType()).isEqualTo(void.class);
    }
}
