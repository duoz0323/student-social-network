package com.stu.edu.vn.backend.admin.rbac.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.admin.rbac.entity.AdminRole;
import com.stu.edu.vn.backend.admin.collaborator.identity.ManagedSocialIdentityService;
import com.stu.edu.vn.backend.admin.rbac.dto.ResetAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRoleRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.ChangeAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateAdminProfileRequest;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignmentId;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminAccountRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminPermissionRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleAssignmentRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.AdminAuthorization;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.impl.UserProfileValidationSupport;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AdminManagementServiceImplTest {

    @Test
    void createAdminRejectsBootstrapSuperAdminRole() {
        UserRepository users = mock(UserRepository.class);
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        PasswordPolicyValidator passwordPolicy = mock(PasswordPolicyValidator.class);
        UserProfileValidationSupport validation = mock(UserProfileValidationSupport.class);
        AdminRole superAdmin = mock(AdminRole.class);
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        when(users.existsByEmail("support@example.com")).thenReturn(false);
        when(passwordPolicy.isValid("Support123!")).thenReturn(true);
        when(validation.normalizeAndValidateUsername("support_admin")).thenReturn("support_admin");
        when(validation.normalizeAndValidateDisplayName("Support Admin")).thenReturn("Support Admin");
        when(validation.validateDateOfBirth(LocalDate.of(2000, 1, 1))).thenReturn(LocalDate.of(2000, 1, 1));
        when(roles.findAllByCodeIn(Set.of("SUPER_ADMIN"))).thenReturn(List.of(superAdmin));
        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, profiles, roles,
                mock(AdminRoleAssignmentRepository.class), mock(AdminPermissionRepository.class),
                mock(DatabaseAdminAuthorityResolver.class), mock(RefreshTokenRepository.class),
                mock(AdminActionRepository.class), mock(CurrentUserProvider.class), passwordPolicy, validation,
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.createAdmin(new CreateAdminRequest(
                "support@example.com", "Support123!", "Support123!", "support_admin", "Support Admin",
                LocalDate.of(2000, 1, 1), Set.of("SUPER_ADMIN"))))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_SUPER_ROLE_BOOTSTRAP_ONLY));
    }

    @Test
    void delegatedRoleCannotReceivePermissionDelegationCapabilities() {
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRole supportRole = mock(AdminRole.class);
        when(supportRole.getCode()).thenReturn("SUPPORT");
        when(roles.findByCodeForUpdate("SUPPORT")).thenReturn(Optional.of(supportRole));
        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), mock(UserRepository.class), mock(UserProfileRepository.class),
                roles, mock(AdminRoleAssignmentRepository.class), mock(AdminPermissionRepository.class),
                mock(DatabaseAdminAuthorityResolver.class), mock(RefreshTokenRepository.class),
                mock(AdminActionRepository.class), mock(CurrentUserProvider.class),
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.updateRolePermissions(
                "SUPPORT", Set.of("DASHBOARD_BASIC_VIEW", "ADMIN_ROLE_ASSIGN")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_PERMISSION_NOT_DELEGABLE));
    }

    @Test
    void updatesCurrentAdminProfileUsingAuthenticatedIdentity() {
        UserRepository users = mock(UserRepository.class);
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        UserProfileValidationSupport validation = mock(UserProfileValidationSupport.class);
        DatabaseAdminAuthorityResolver authorities = mock(DatabaseAdminAuthorityResolver.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        User admin = new User("admin@example.com", "hash");
        ReflectionTestUtils.setField(admin, "id", 7L);
        admin.setRole(UserRole.ADMIN);
        UserProfile profile = new UserProfile(admin);
        LocalDate birthDate = LocalDate.of(1999, 5, 20);
        when(currentUser.getCurrentUserId()).thenReturn(7L);
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(admin));
        when(users.findById(7L)).thenReturn(Optional.of(admin));
        when(profiles.findByIdForUpdate(7L)).thenReturn(Optional.of(profile));
        when(profiles.findById(7L)).thenReturn(Optional.of(profile));
        when(validation.normalizeAndValidateDisplayName(" Quản trị viên ")).thenReturn("Quản trị viên");
        when(validation.validateDateOfBirth(birthDate)).thenReturn(birthDate);
        when(validation.normalizeAndValidateBio(" Giới thiệu ")).thenReturn("Giới thiệu");
        when(authorities.resolve(7L)).thenReturn(new AdminAuthorization(Set.of("MODERATOR"), Set.of("DASHBOARD_BASIC_VIEW")));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, profiles, mock(AdminRoleRepository.class),
                mock(AdminRoleAssignmentRepository.class), mock(AdminPermissionRepository.class), authorities,
                mock(RefreshTokenRepository.class), actions, currentUser, mock(PasswordPolicyValidator.class),
                validation, mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        var result = service.updateCurrentAdminProfile(
                new UpdateAdminProfileRequest(" Quản trị viên ", birthDate, " Giới thiệu "));

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.displayName()).isEqualTo("Quản trị viên");
        assertThat(result.bio()).isEqualTo("Giới thiệu");
        verify(actions).save(any());
    }

    @Test
    void changesCurrentAdminPasswordAndRevokesEverySession() {
        UserRepository users = mock(UserRepository.class);
        RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        PasswordPolicyValidator passwordPolicy = mock(PasswordPolicyValidator.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        User admin = new User("admin@example.com", "old-hash");
        ReflectionTestUtils.setField(admin, "id", 7L);
        admin.setRole(UserRole.ADMIN);
        when(currentUser.getCurrentUserId()).thenReturn(7L);
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(admin));
        when(users.findById(7L)).thenReturn(Optional.of(admin));
        when(encoder.matches("Current123!", "old-hash")).thenReturn(true);
        when(encoder.matches("NewPassword123!", "old-hash")).thenReturn(false);
        when(passwordPolicy.isValid("NewPassword123!")).thenReturn(true);
        when(encoder.encode("NewPassword123!")).thenReturn("new-hash");

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class),
                mock(AdminRoleRepository.class), mock(AdminRoleAssignmentRepository.class),
                mock(AdminPermissionRepository.class), mock(DatabaseAdminAuthorityResolver.class), refreshTokens,
                actions, currentUser, passwordPolicy, mock(UserProfileValidationSupport.class), encoder,
                mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        service.changeCurrentAdminPassword(
                new ChangeAdminPasswordRequest("Current123!", "NewPassword123!", "NewPassword123!"));

        assertThat(admin.getPasswordHash()).isEqualTo("new-hash");
        verify(refreshTokens).revokeAllActiveByUserId(org.mockito.ArgumentMatchers.eq(7L), any());
        verify(actions).save(any());
    }

    @Test
    void rejectsIncorrectCurrentAdminPassword() {
        UserRepository users = mock(UserRepository.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        User admin = new User("admin@example.com", "old-hash");
        ReflectionTestUtils.setField(admin, "id", 7L);
        admin.setRole(UserRole.ADMIN);
        when(currentUser.getCurrentUserId()).thenReturn(7L);
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(admin));
        when(encoder.matches("Wrong123!", "old-hash")).thenReturn(false);
        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class),
                mock(AdminRoleRepository.class), mock(AdminRoleAssignmentRepository.class),
                mock(AdminPermissionRepository.class), mock(DatabaseAdminAuthorityResolver.class),
                mock(RefreshTokenRepository.class), mock(AdminActionRepository.class), currentUser,
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class), encoder,
                mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.changeCurrentAdminPassword(
                new ChangeAdminPasswordRequest("Wrong123!", "NewPassword123!", "NewPassword123!")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.AUTH_CURRENT_PASSWORD_INVALID));
    }

    @Test
    void createsCustomRoleWithGeneratedCodeAndDashboardPermission() {
        UserRepository users = mock(UserRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminPermissionRepository permissions = mock(AdminPermissionRepository.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        User actor = new User("root@example.com", "hash");
        ReflectionTestUtils.setField(actor, "id", 1L);
        when(roles.existsByCode("QUAN_LY_SU_KIEN")).thenReturn(false);
        when(roles.saveAndFlush(any(AdminRole.class))).thenAnswer(invocation -> {
            AdminRole role = invocation.getArgument(0);
            ReflectionTestUtils.setField(role, "id", 9L);
            return role;
        });
        when(permissions.findAllByCodeIn(Set.of("DASHBOARD_BASIC_VIEW")))
                .thenReturn(List.of(mock(com.stu.edu.vn.backend.admin.rbac.entity.AdminPermissionEntity.class)));
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(users.findById(1L)).thenReturn(Optional.of(actor));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class), roles,
                mock(AdminRoleAssignmentRepository.class), permissions, mock(DatabaseAdminAuthorityResolver.class),
                mock(RefreshTokenRepository.class), actions, currentUser, mock(PasswordPolicyValidator.class),
                mock(UserProfileValidationSupport.class), mock(PasswordEncoder.class),
                mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        var result = service.createRole(new CreateAdminRoleRequest("  Quản lý   sự kiện  "));

        assertThat(result.code()).isEqualTo("QUAN_LY_SU_KIEN");
        assertThat(result.displayName()).isEqualTo("Quản lý sự kiện");
        assertThat(result.reserved()).isFalse();
        assertThat(result.permissions()).containsExactly("DASHBOARD_BASIC_VIEW");
        verify(permissions).insertMappings(9L, Set.of("DASHBOARD_BASIC_VIEW"));
        verify(actions).save(any());
    }

    @Test
    void rejectsDuplicateGeneratedRoleCode() {
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        when(roles.existsByCode("QUAN_LY_SU_KIEN")).thenReturn(true);
        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), mock(UserRepository.class), mock(UserProfileRepository.class),
                roles, mock(AdminRoleAssignmentRepository.class), mock(AdminPermissionRepository.class),
                mock(DatabaseAdminAuthorityResolver.class), mock(RefreshTokenRepository.class),
                mock(AdminActionRepository.class), mock(CurrentUserProvider.class),
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.createRole(new CreateAdminRoleRequest("Quản lý sự kiện")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_ROLE_ALREADY_EXISTS));
    }

    @Test
    void resetsAdminPasswordRevokesSessionsAndWritesAudit() {
        UserRepository users = mock(UserRepository.class);
        RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        PasswordPolicyValidator passwordPolicy = mock(PasswordPolicyValidator.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User target = new User("admin@example.com", "old-hash");
        ReflectionTestUtils.setField(target, "id", 12L);
        target.setRole(UserRole.ADMIN);
        User actor = new User("root@example.com", "actor-hash");
        ReflectionTestUtils.setField(actor, "id", 1L);
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(target));
        when(users.findById(1L)).thenReturn(Optional.of(actor));
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(passwordPolicy.isValid("NewAdmin123!" )).thenReturn(true);
        when(passwordEncoder.matches("NewAdmin123!", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewAdmin123!")).thenReturn("new-hash");

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class),
                mock(AdminRoleRepository.class), mock(AdminRoleAssignmentRepository.class),
                mock(AdminPermissionRepository.class), mock(DatabaseAdminAuthorityResolver.class), refreshTokens,
                actions, currentUser, passwordPolicy, mock(UserProfileValidationSupport.class), passwordEncoder,
                mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        service.resetPassword(12L, new ResetAdminPasswordRequest("NewAdmin123!", "NewAdmin123!"));

        assertThat(target.getPasswordHash()).isEqualTo("new-hash");
        verify(refreshTokens).revokeAllActiveByUserId(org.mockito.ArgumentMatchers.eq(12L), any());
        verify(actions).save(any());
    }

    @Test
    void resetAdminPasswordRejectsMismatchedConfirmation() {
        UserRepository users = mock(UserRepository.class);
        User target = new User("admin@example.com", "old-hash");
        ReflectionTestUtils.setField(target, "id", 12L);
        target.setRole(UserRole.ADMIN);
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(target));
        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class),
                mock(AdminRoleRepository.class), mock(AdminRoleAssignmentRepository.class),
                mock(AdminPermissionRepository.class), mock(DatabaseAdminAuthorityResolver.class),
                mock(RefreshTokenRepository.class), mock(AdminActionRepository.class), mock(CurrentUserProvider.class),
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.resetPassword(
                12L, new ResetAdminPasswordRequest("NewAdmin123!", "Different123!")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH));
    }

    @Test
    void enablesBlockedAdminAndWritesAudit() {
        UserRepository users = mock(UserRepository.class);
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        DatabaseAdminAuthorityResolver authorityResolver = mock(DatabaseAdminAuthorityResolver.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        User target = new User("blocked-admin@example.com", "hash");
        ReflectionTestUtils.setField(target, "id", 12L);
        target.setRole(UserRole.ADMIN);
        target.setStatus(UserStatus.BLOCKED);
        target.setBlockedAt(java.time.LocalDateTime.now());
        target.setBlockedReason("ADMIN_DISABLED");
        User actor = new User("root@example.com", "hash");
        ReflectionTestUtils.setField(actor, "id", 1L);
        UserProfile profile = mock(UserProfile.class);
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(target));
        when(users.findById(1L)).thenReturn(Optional.of(actor));
        when(profiles.findById(12L)).thenReturn(Optional.of(profile));
        when(authorityResolver.resolve(12L)).thenReturn(new AdminAuthorization(Set.of("MODERATOR"), Set.of()));
        when(currentUser.getCurrentUserId()).thenReturn(1L);

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, profiles, mock(AdminRoleRepository.class),
                mock(AdminRoleAssignmentRepository.class), mock(AdminPermissionRepository.class), authorityResolver,
                mock(RefreshTokenRepository.class), actions, currentUser, mock(PasswordPolicyValidator.class),
                mock(UserProfileValidationSupport.class), mock(PasswordEncoder.class),
                mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        var result = service.enableAdmin(12L);

        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(target.getBlockedAt()).isNull();
        assertThat(target.getBlockedReason()).isNull();
        verify(actions).save(any());
    }

    @Test
    void updatesRolePermissionSnapshotAndRevokesAssignedAdminSessions() {
        UserRepository users = mock(UserRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRoleAssignmentRepository assignments = mock(AdminRoleAssignmentRepository.class);
        AdminPermissionRepository permissions = mock(AdminPermissionRepository.class);
        RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        AdminRole moderator = mock(AdminRole.class);
        when(moderator.getId()).thenReturn(3L);
        when(moderator.getCode()).thenReturn("EVENT_MANAGER");
        when(moderator.getDisplayName()).thenReturn("Quản lý sự kiện");
        when(roles.findByCodeForUpdate("EVENT_MANAGER")).thenReturn(Optional.of(moderator));
        Set<String> nextCodes = Set.of("DASHBOARD_BASIC_VIEW", "REPORT_VIEW");
        when(permissions.findAllByCodeIn(nextCodes)).thenReturn(List.of(
                mock(com.stu.edu.vn.backend.admin.rbac.entity.AdminPermissionEntity.class),
                mock(com.stu.edu.vn.backend.admin.rbac.entity.AdminPermissionEntity.class)));
        when(permissions.findCodesByRoleId(3L)).thenReturn(List.of("DASHBOARD_BASIC_VIEW", "POST_VIEW"));
        when(assignments.findAdminIdsByRoleId(3L)).thenReturn(List.of(10L, 11L));
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(users.findById(1L)).thenReturn(Optional.of(new User("root@example.com", "hash")));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class), roles, assignments,
                permissions, mock(DatabaseAdminAuthorityResolver.class), refreshTokens, actions, currentUser,
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        var result = service.updateRolePermissions("event_manager", nextCodes);

        assertThat(result.permissions()).containsExactlyInAnyOrderElementsOf(nextCodes);
        verify(permissions).deleteMappingsByRoleId(3L);
        verify(permissions).insertMappings(3L, nextCodes);
        verify(refreshTokens).revokeAllActiveByUserId(org.mockito.ArgumentMatchers.eq(10L), any());
        verify(refreshTokens).revokeAllActiveByUserId(org.mockito.ArgumentMatchers.eq(11L), any());
        verify(actions).save(any());
    }

    @Test
    void collaboratorSystemPermissionBundleCannotBeEdited() {
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRole collaborator = mock(AdminRole.class);
        when(collaborator.getCode()).thenReturn("COLLABORATOR");
        when(roles.findByCodeForUpdate("COLLABORATOR")).thenReturn(Optional.of(collaborator));
        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), mock(UserRepository.class), mock(UserProfileRepository.class),
                roles, mock(AdminRoleAssignmentRepository.class), mock(AdminPermissionRepository.class),
                mock(DatabaseAdminAuthorityResolver.class), mock(RefreshTokenRepository.class),
                mock(AdminActionRepository.class), mock(CurrentUserProvider.class),
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.updateRolePermissions("COLLABORATOR", Set.of("POST_VIEW")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_COLLABORATOR_PERMISSIONS_IMMUTABLE));
    }

    @Test
    void assigningCollaboratorRoleActivatesManagedIdentityInTheSameServiceFlow() {
        UserRepository users = mock(UserRepository.class);
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRoleAssignmentRepository assignments = mock(AdminRoleAssignmentRepository.class);
        DatabaseAdminAuthorityResolver authorities = mock(DatabaseAdminAuthorityResolver.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        ManagedSocialIdentityService identities = mock(ManagedSocialIdentityService.class);
        User target = new User("collaborator@example.com", "hash");
        ReflectionTestUtils.setField(target, "id", 15L);
        target.setRole(UserRole.ADMIN);
        User actor = new User("master@example.com", "hash");
        ReflectionTestUtils.setField(actor, "id", 1L);
        UserProfile profile = new UserProfile(target);
        AdminRole collaborator = mock(AdminRole.class);
        when(collaborator.getId()).thenReturn(5L);
        when(collaborator.getCode()).thenReturn("COLLABORATOR");
        when(users.findByIdForUpdate(15L)).thenReturn(Optional.of(target));
        when(users.findById(1L)).thenReturn(Optional.of(actor));
        when(profiles.findById(15L)).thenReturn(Optional.of(profile));
        when(roles.findByCode("COLLABORATOR")).thenReturn(Optional.of(collaborator));
        when(assignments.findRoleCodes(15L)).thenReturn(List.of());
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(authorities.resolve(15L)).thenReturn(new AdminAuthorization(
                Set.of("COLLABORATOR"), Set.of("COLLABORATOR_DASHBOARD_VIEW")));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, profiles, roles, assignments,
                mock(AdminPermissionRepository.class), authorities, mock(RefreshTokenRepository.class),
                mock(AdminActionRepository.class), currentUser, mock(PasswordPolicyValidator.class),
                mock(UserProfileValidationSupport.class), mock(PasswordEncoder.class), identities, Clock.systemUTC());

        var response = service.assignRole(15L, "COLLABORATOR");

        assertThat(response.roles()).contains("COLLABORATOR");
        verify(identities).activateOrCreateForRole(target, actor);
        verify(assignments).save(any());
    }

    @Test
    void revokingCollaboratorRoleDisablesIdentityWithoutDeletingSocialData() {
        UserRepository users = mock(UserRepository.class);
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRoleAssignmentRepository assignments = mock(AdminRoleAssignmentRepository.class);
        DatabaseAdminAuthorityResolver authorities = mock(DatabaseAdminAuthorityResolver.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        ManagedSocialIdentityService identities = mock(ManagedSocialIdentityService.class);
        User target = new User("collaborator@example.com", "hash");
        ReflectionTestUtils.setField(target, "id", 15L);
        target.setRole(UserRole.ADMIN);
        User actor = new User("master@example.com", "hash");
        ReflectionTestUtils.setField(actor, "id", 1L);
        UserProfile profile = new UserProfile(target);
        AdminRole collaborator = mock(AdminRole.class);
        when(collaborator.getId()).thenReturn(5L);
        when(collaborator.getCode()).thenReturn("COLLABORATOR");
        when(users.findByIdForUpdate(15L)).thenReturn(Optional.of(target));
        when(users.findById(1L)).thenReturn(Optional.of(actor));
        when(profiles.findById(15L)).thenReturn(Optional.of(profile));
        when(roles.findByCode("COLLABORATOR")).thenReturn(Optional.of(collaborator));
        when(assignments.findRoleCodes(15L)).thenReturn(List.of("COLLABORATOR", "MODERATOR"));
        when(assignments.findByIdForUpdate(new AdminRoleAssignmentId(15L, 5L)))
                .thenReturn(Optional.of(new com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignment(15L, 5L, 1L)));
        when(assignments.countByIdAdminId(15L)).thenReturn(2L);
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(authorities.resolve(15L)).thenReturn(new AdminAuthorization(Set.of("MODERATOR"), Set.of()));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, profiles, roles, assignments,
                mock(AdminPermissionRepository.class), authorities, mock(RefreshTokenRepository.class),
                mock(AdminActionRepository.class), currentUser, mock(PasswordPolicyValidator.class),
                mock(UserProfileValidationSupport.class), mock(PasswordEncoder.class), identities, Clock.systemUTC());

        service.revokeRole(15L, "COLLABORATOR");

        verify(identities).disableForRoleRevocation(15L, actor);
        verify(assignments).delete(any());
    }

    @Test
    // Master Admin bất biến ngay cả khi dữ liệu cũ vô tình có thêm SUPER_ADMIN đang hoạt động.
    void masterAdminCannotBeDisabledEvenWhenAnotherSuperAdminIsActive() {
        UserRepository users = mock(UserRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRoleAssignmentRepository assignments = mock(AdminRoleAssignmentRepository.class);
        User target = new User("root@example.com", "hash");
        ReflectionTestUtils.setField(target, "id", 1L);
        target.setRole(UserRole.ADMIN);
        target.setStatus(UserStatus.ACTIVE);
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(target));
        when(assignments.findRoleCodes(1L)).thenReturn(List.of("SUPER_ADMIN"));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class), roles, assignments,
                mock(AdminPermissionRepository.class), mock(DatabaseAdminAuthorityResolver.class),
                mock(RefreshTokenRepository.class), mock(AdminActionRepository.class), mock(CurrentUserProvider.class),
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.disableAdmin(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_MASTER_ACCOUNT_PROTECTED));
    }

    @Test
    // Không cho dùng API gán role nghiệp vụ để thay đổi tài khoản quản trị gốc.
    void masterAdminCannotReceiveOrLoseBusinessRole() {
        UserRepository users = mock(UserRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRoleAssignmentRepository assignments = mock(AdminRoleAssignmentRepository.class);
        User target = new User("root@example.com", "hash");
        ReflectionTestUtils.setField(target, "id", 1L);
        target.setRole(UserRole.ADMIN);
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(target));
        when(assignments.findRoleCodes(1L)).thenReturn(List.of("SUPER_ADMIN"));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class), roles, assignments,
                mock(AdminPermissionRepository.class), mock(DatabaseAdminAuthorityResolver.class),
                mock(RefreshTokenRepository.class), mock(AdminActionRepository.class), mock(CurrentUserProvider.class),
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.assignRole(1L, "MODERATOR"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_MASTER_ACCOUNT_PROTECTED));
        assertThatThrownBy(() -> service.revokeRole(1L, "MODERATOR"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_MASTER_ACCOUNT_PROTECTED));
    }

    @Test
    // Mật khẩu Master Admin chỉ được đổi bằng luồng tự quản lý trong hồ sơ.
    void masterAdminPasswordCannotBeResetFromAdminManagement() {
        UserRepository users = mock(UserRepository.class);
        AdminRoleRepository roles = mock(AdminRoleRepository.class);
        AdminRoleAssignmentRepository assignments = mock(AdminRoleAssignmentRepository.class);
        User target = new User("root@example.com", "old-hash");
        ReflectionTestUtils.setField(target, "id", 1L);
        target.setRole(UserRole.ADMIN);
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(target));
        when(assignments.findRoleCodes(1L)).thenReturn(List.of("SUPER_ADMIN"));

        AdminManagementServiceImpl service = new AdminManagementServiceImpl(
                mock(AdminAccountRepository.class), users, mock(UserProfileRepository.class), roles, assignments,
                mock(AdminPermissionRepository.class), mock(DatabaseAdminAuthorityResolver.class),
                mock(RefreshTokenRepository.class), mock(AdminActionRepository.class), mock(CurrentUserProvider.class),
                mock(PasswordPolicyValidator.class), mock(UserProfileValidationSupport.class),
                mock(PasswordEncoder.class), mock(ManagedSocialIdentityService.class), Clock.systemUTC());

        assertThatThrownBy(() -> service.resetPassword(
                1L, new ResetAdminPasswordRequest("NewAdmin123!", "NewAdmin123!")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADMIN_MASTER_ACCOUNT_PROTECTED));
    }
}
