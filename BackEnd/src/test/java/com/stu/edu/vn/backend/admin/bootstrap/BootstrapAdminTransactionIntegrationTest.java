package com.stu.edu.vn.backend.admin.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Test tích hợp tùy chọn dùng MySQL thật để chứng minh lỗi user_profiles rollback bản ghi users.
 */
@SpringBootTest(properties = "bootstrap-admin.enabled=false")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
@Import(BootstrapAdminTransactionIntegrationTest.FailingProfileRepositoryConfiguration.class)
class BootstrapAdminTransactionIntegrationTest {

    @Autowired
    private BootstrapAdminProperties properties;

    @Autowired
    private BootstrapAdminService service;

    @Autowired
    private UserRepository userRepository;

    @Test
    void profileFailureRollsBackPreviouslyFlushedUser() {
        // Email ngẫu nhiên tránh xung đột với dữ liệu có sẵn trong database tích hợp của từng máy.
        String testEmail = "rollback-bootstrap-" + UUID.randomUUID() + "@example.com";
        properties.setEnabled(true);
        properties.setEmail(testEmail);
        properties.setPassword("AdminPassword@1");
        properties.setDisplayName("Rollback Admin");
        assertThat(userRepository.existsByEmail(testEmail)).isFalse();

        org.assertj.core.api.Assertions.assertThatThrownBy(service::bootstrapIfEnabled)
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(userRepository.existsByEmail(testEmail)).isFalse();
    }

    @TestConfiguration
    static class FailingProfileRepositoryConfiguration {

        @Bean
        @Primary
        UserProfileRepository failingUserProfileRepository() {
            UserProfileRepository repository = org.mockito.Mockito.mock(UserProfileRepository.class);
            when(repository.saveAndFlush(any(UserProfile.class)))
                    .thenThrow(new DataIntegrityViolationException("profile failed intentionally"));
            return repository;
        }
    }
}
