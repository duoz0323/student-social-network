package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.dto.request.AdminUpdateUserProfileRequest;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminAvatarAction;
import com.stu.edu.vn.backend.admin.mapper.AdminUserMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminUserDetailProjection;
import com.stu.edu.vn.backend.admin.repository.AdminUserListProjection;
import com.stu.edu.vn.backend.admin.repository.AdminUserRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.service.impl.UserProfileValidationSupport;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import com.stu.edu.vn.backend.storage.CloudinaryUploadResult;
import com.stu.edu.vn.backend.user.service.impl.UserAvatarFileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.mock.web.MockMultipartFile;

class AdminUserServiceImplTest {

    private final AdminUserRepository adminUserRepository = org.mockito.Mockito.mock(AdminUserRepository.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final AdminActionRepository adminActionRepository = org.mockito.Mockito.mock(AdminActionRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final CloudinaryStorageService cloudinaryStorageService = org.mockito.Mockito.mock(CloudinaryStorageService.class);
    private final TransactionTemplate transactionTemplate = org.mockito.Mockito.mock(TransactionTemplate.class);
    private final NotificationService notificationService = org.mockito.Mockito.mock(NotificationService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-30T01:00:00Z"), ZoneOffset.UTC);
    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(
                adminUserRepository,
                new AdminUserMapper(),
                currentUserProvider,
                org.mockito.Mockito.mock(com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository.class),
                org.mockito.Mockito.mock(com.stu.edu.vn.backend.admin.repository.AccountStatusHistoryRepository.class),
                adminActionRepository,
                clock,
                entityManager,
                notificationService,
                userProfileRepository,
                new UserProfileValidationSupport(clock),
                cloudinaryStorageService,
                new UserAvatarFileValidator(),
                transactionTemplate
        );
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(org.mockito.Mockito.mock(TransactionStatus.class));
        });
    }

    @Test
    void listTrimsEscapesKeywordFiltersStatusAndMapsProjection() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 14, 8, 0);
        AdminUserListProjection projection = listProjection(10L, "Minh", "ACTIVE", createdAt, true);
        when(adminUserRepository.findManagedUsers(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(0, 20), 1));

        var response = adminUserService.getUsers("  50%_off=now  ", UserStatus.ACTIVE, 0, 20);

        verify(adminUserRepository).findManagedUsers("50=%=_off==now", "ACTIVE", PageRequest.of(0, 20));
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst())
                .extracting("userId", "displayName", "status", "profileCompleted", "createdAt")
                .containsExactly(10L, "Minh", UserStatus.ACTIVE, true, createdAt);
    }

    @Test
    void listTreatsMissingAndBlankKeywordAsNull() {
        when(adminUserRepository.findManagedUsers(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        adminUserService.getUsers(null, null, 0, 20);
        adminUserService.getUsers("   ", null, 0, 20);

        verify(adminUserRepository, org.mockito.Mockito.times(2))
                .findManagedUsers(null, null, PageRequest.of(0, 20));
    }

    @Test
    void listAllowsBoundarySizesAndEmptyPage() {
        when(adminUserRepository.findManagedUsers(any(), any(), any()))
                .thenAnswer(invocation -> {
                    PageRequest request = invocation.getArgument(2);
                    return new PageImpl<AdminUserListProjection>(List.of(), request, 0);
                });

        var sizeOne = adminUserService.getUsers(null, null, 0, 1);
        var sizeOneHundred = adminUserService.getUsers(null, null, 2, 100);

        assertThat(sizeOne.content()).isEmpty();
        assertThat(sizeOne.size()).isEqualTo(1);
        assertThat(sizeOneHundred.content()).isEmpty();
        assertThat(sizeOneHundred.size()).isEqualTo(100);
    }

    @Test
    void listRejectsInvalidPaginationAndOverlongKeywordBeforeQuery() {
        assertError(() -> adminUserService.getUsers(null, null, -1, 20), ErrorCode.VALIDATION_ERROR);
        assertError(() -> adminUserService.getUsers(null, null, 0, 0), ErrorCode.VALIDATION_ERROR);
        assertError(() -> adminUserService.getUsers(null, null, 0, 101), ErrorCode.VALIDATION_ERROR);
        assertError(() -> adminUserService.getUsers("a".repeat(101), null, 0, 20), ErrorCode.SEARCH_KEYWORD_TOO_LONG);
        verify(adminUserRepository, never()).findManagedUsers(any(), any(), any());
    }

    @Test
    void detailReturnsUserWithCompletedProfileAndSafeAccountFields() {
        AdminUserDetailProjection projection = detailProjection("USER", "BLOCKED", true);
        when(adminUserRepository.findManagedUserDetail(10L)).thenReturn(Optional.of(projection));

        var response = adminUserService.getUserDetail(10L);

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(UserStatus.BLOCKED);
        assertThat(response.profileCompleted()).isTrue();
        assertThat(response.blockedReason()).isEqualTo("SPAM");
    }

    @Test
    void detailAllowsIncompleteProfile() {
        AdminUserDetailProjection projection = detailProjection("USER", "ACTIVE", false);
        when(adminUserRepository.findManagedUserDetail(10L))
                .thenReturn(Optional.of(projection));

        var response = adminUserService.getUserDetail(10L);

        assertThat(response.profileCompleted()).isFalse();
        assertThat(response.profileCompletedAt()).isNull();
    }

    @Test
    void detailRejectsMissingAndAdminTargetWithApprovedErrors() {
        AdminUserDetailProjection adminProjection = detailProjection("ADMIN", "ACTIVE", true);
        when(adminUserRepository.findManagedUserDetail(404L)).thenReturn(Optional.empty());
        when(adminUserRepository.findManagedUserDetail(1L))
                .thenReturn(Optional.of(adminProjection));

        assertError(() -> adminUserService.getUserDetail(404L), ErrorCode.ADMIN_USER_NOT_FOUND);
        assertError(() -> adminUserService.getUserDetail(1L), ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);
    }

    @Test
    void updateProfileValidatesUpdatesAndAuditsManagedUser() {
        User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        UserProfile profile = new UserProfile(target);
        AdminUserDetailProjection updatedProjection = detailProjection("USER", "ACTIVE", true);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));
        when(userProfileRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(profile));
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);
        when(adminUserRepository.findManagedUserDetail(10L)).thenReturn(Optional.of(updatedProjection));

        var response = adminUserService.updateUserProfile(10L,
                new AdminUpdateUserProfileRequest("  Tên mới  ", LocalDate.of(2001, 6, 15), "  Bio mới  "));

        assertThat(profile)
                .extracting(UserProfile::getDisplayName, UserProfile::getDateOfBirth, UserProfile::getBio)
                .containsExactly("Tên mới", LocalDate.of(2001, 6, 15), "Bio mới");
        assertThat(response.userId()).isEqualTo(10L);
        ArgumentCaptor<AdminAction> actionCaptor = ArgumentCaptor.forClass(AdminAction.class);
        verify(adminActionRepository).save(actionCaptor.capture());
        assertThat(actionCaptor.getValue())
                .extracting(AdminAction::getActionType, AdminAction::getTargetId, AdminAction::getNote)
                .containsExactly(AdminActionType.UPDATE_USER_PROFILE, 10L, "ADMIN_UPDATE_PROFILE");
        verify(entityManager).flush();
        verify(notificationService).createUserProfileUpdatedByAdminNotification(10L);
    }

    @Test
    void updateProfileWithAvatarReplacesImageAndDeletesOldFileAfterDatabaseSuccess() {
        User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        UserProfile profile = new UserProfile(target);
        profile.setAvatarUrl("https://cdn.example/old.png");
        profile.setAvatarPublicId("avatars/old");
        AdminUserDetailProjection updatedProjection = detailProjection("USER", "ACTIVE", true);
        when(updatedProjection.getAvatarUrl()).thenReturn("https://cdn.example/new.png");
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(adminUserRepository.findManagedUserDetail(10L)).thenReturn(Optional.of(updatedProjection));
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));
        when(userProfileRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(profile));
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);
        when(cloudinaryStorageService.uploadAvatar(any())).thenReturn(
                new CloudinaryUploadResult("https://cdn.example/new.png", "avatars/new"));
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", pngHeader);

        var response = adminUserService.updateUserProfileWithAvatar(
                10L,
                new AdminUpdateUserProfileRequest("Tên mới", LocalDate.of(2001, 6, 15), "Bio mới"),
                AdminAvatarAction.REPLACE,
                avatar
        );

        assertThat(profile.getAvatarUrl()).isEqualTo("https://cdn.example/new.png");
        assertThat(profile.getAvatarPublicId()).isEqualTo("avatars/new");
        assertThat(response.avatarUrl()).isEqualTo("https://cdn.example/new.png");
        verify(cloudinaryStorageService).deleteImage("avatars/old");
        verify(notificationService).createUserProfileUpdatedByAdminNotification(10L);
    }

    @Test
    void updateProfileWithAvatarRemovesCurrentImageWithoutUploadingNewFile() {
        User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
        User target = user(10L, UserRole.USER, UserStatus.ACTIVE);
        UserProfile profile = new UserProfile(target);
        profile.setAvatarUrl("https://cdn.example/old.png");
        profile.setAvatarPublicId("avatars/old");
        AdminUserDetailProjection updatedProjection = detailProjection("USER", "ACTIVE", true);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(adminUserRepository.findManagedUserDetail(10L)).thenReturn(Optional.of(updatedProjection));
        when(adminUserRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(target));
        when(userProfileRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(profile));
        when(entityManager.getReference(User.class, 1L)).thenReturn(admin);

        var response = adminUserService.updateUserProfileWithAvatar(
                10L,
                new AdminUpdateUserProfileRequest("Tên mới", LocalDate.of(2001, 6, 15), "Bio mới"),
                AdminAvatarAction.REMOVE,
                null
        );

        assertThat(profile.getAvatarUrl()).isNull();
        assertThat(profile.getAvatarPublicId()).isNull();
        assertThat(response.avatarUrl()).isNull();
        verify(cloudinaryStorageService, never()).uploadAvatar(any());
        verify(cloudinaryStorageService).deleteImage("avatars/old");
        verify(notificationService).createUserProfileUpdatedByAdminNotification(10L);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private AdminUserListProjection listProjection(
            Long userId,
            String displayName,
            String status,
            LocalDateTime createdAt,
            boolean completed
    ) {
        AdminUserListProjection projection = org.mockito.Mockito.mock(AdminUserListProjection.class);
        when(projection.getUserId()).thenReturn(userId);
        when(projection.getDisplayName()).thenReturn(displayName);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getCreatedAt()).thenReturn(createdAt);
        when(projection.getProfileCompletedAt()).thenReturn(completed ? createdAt : null);
        return projection;
    }

    private AdminUserDetailProjection detailProjection(String role, String status, boolean completed) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 14, 8, 0);
        AdminUserDetailProjection projection = org.mockito.Mockito.mock(AdminUserDetailProjection.class);
        when(projection.getUserId()).thenReturn(10L);
        when(projection.getRole()).thenReturn(role);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getDisplayName()).thenReturn("Minh");
        when(projection.getDateOfBirth()).thenReturn(LocalDate.of(2001, 6, 15));
        when(projection.getEmail()).thenReturn("minh@example.com");
        when(projection.getProfileCompletedAt()).thenReturn(completed ? timestamp : null);
        when(projection.getBlockedAt()).thenReturn("BLOCKED".equals(status) ? timestamp : null);
        when(projection.getBlockedReason()).thenReturn("BLOCKED".equals(status) ? "SPAM" : null);
        when(projection.getCreatedAt()).thenReturn(timestamp);
        when(projection.getUpdatedAt()).thenReturn(timestamp);
        return projection;
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User("user" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
