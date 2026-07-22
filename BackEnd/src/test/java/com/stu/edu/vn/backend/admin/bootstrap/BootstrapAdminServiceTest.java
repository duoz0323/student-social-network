package com.stu.edu.vn.backend.admin.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

class BootstrapAdminServiceTest {

    private static final String RAW_PASSWORD = "AdminPassword@1";
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-14T03:00:00Z"),
            ZoneId.of("Asia/Ho_Chi_Minh")
    );

    private final BootstrapAdminProperties properties = new BootstrapAdminProperties();
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private BootstrapAdminService service;

    @BeforeEach
    void setUp() {
        properties.setEnabled(true);
        properties.setEmail(" Admin@Example.COM ");
        properties.setPassword(RAW_PASSWORD);
        properties.setDisplayName("  Quản trị viên hệ thống  ");
        service = new BootstrapAdminService(
                properties,
                userRepository,
                userProfileRepository,
                passwordEncoder,
                CLOCK
        );
    }

    @Test
    void bootstrapCreatesActiveAdminWithHashedPassword() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.saveAndFlush(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BootstrapAdminResult result = service.bootstrapIfEnabled();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        verify(userProfileRepository).saveAndFlush(profileCaptor.capture());

        User admin = userCaptor.getValue();
        UserProfile profile = profileCaptor.getValue();
        assertThat(result).isEqualTo(BootstrapAdminResult.CREATED);
        assertThat(admin.getEmail()).isEqualTo("admin@example.com");
        assertThat(admin.getEmailVerifiedAt()).isEqualTo(LocalDateTime.now(CLOCK));
        assertThat(admin.getPasswordHash()).isNotEqualTo(RAW_PASSWORD);
        assertThat(passwordEncoder.matches(RAW_PASSWORD, admin.getPasswordHash())).isTrue();
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(admin.getBlockedAt()).isNull();
        assertThat(admin.getBlockedReason()).isNull();
        assertThat(profile.getUser()).isSameAs(admin);
        assertThat(profile.getDisplayName()).isEqualTo("Quản trị viên hệ thống");
        assertThat(profile.getProfileCompletedAt()).isEqualTo(LocalDateTime.now(CLOCK));
        assertThat(profile.getAvatarUrl()).isNull();
        assertThat(profile.getAvatarPublicId()).isNull();
        assertThat(profile.getBio()).isNull();
        assertThat(profile.getDateOfBirth()).isNull();
    }

    @Test
    void bootstrapSkipsExistingAccountWithoutChangingRoleOrPassword() {
        User existingUser = new User("admin@example.com", "existing-password-hash");
        existingUser.setRole(UserRole.USER);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

        BootstrapAdminResult result = service.bootstrapIfEnabled();

        assertThat(result).isEqualTo(BootstrapAdminResult.ALREADY_EXISTS);
        assertThat(existingUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(existingUser.getPasswordHash()).isEqualTo("existing-password-hash");
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(userProfileRepository);
    }

    @Test
    void bootstrapDoesNothingWhenDisabled() {
        properties.setEnabled(false);

        BootstrapAdminResult result = service.bootstrapIfEnabled();

        assertThat(result).isEqualTo(BootstrapAdminResult.DISABLED);
        verifyNoInteractions(userRepository, userProfileRepository);
    }

    @Test
    void bootstrapRejectsInvalidConfigurationWithoutLoggingOrSavingSensitiveData() {
        properties.setPassword("weak-password");

        assertThatThrownBy(service::bootstrapIfEnabled)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BOOTSTRAP_ADMIN_PASSWORD")
                .hasMessageNotContaining("weak-password");

        verifyNoInteractions(userRepository, userProfileRepository);
    }

    @Test
    void bootstrapPropagatesProfileFailureSoTransactionalProxyCanRollbackUser() throws NoSuchMethodException {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.saveAndFlush(any(UserProfile.class)))
                .thenThrow(new DataIntegrityViolationException("profile failed"));

        assertThatThrownBy(service::bootstrapIfEnabled)
                .isInstanceOf(DataIntegrityViolationException.class);

        // Annotation transaction cùng việc không nuốt RuntimeException bảo đảm Spring rollback thao tác users đã flush.
        Transactional transactional = BootstrapAdminService.class
                .getMethod("bootstrapIfEnabled")
                .getAnnotation(Transactional.class);
        assertThat(transactional).isNotNull();
        verify(userRepository).saveAndFlush(any(User.class));
        verify(userProfileRepository).saveAndFlush(any(UserProfile.class));
    }
}
