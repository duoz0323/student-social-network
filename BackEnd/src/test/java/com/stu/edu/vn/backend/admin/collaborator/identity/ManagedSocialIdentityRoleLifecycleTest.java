package com.stu.edu.vn.backend.admin.collaborator.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleAssignmentRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.impl.UserProfileValidationSupport;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ManagedSocialIdentityRoleLifecycleTest {

    @Test
    void updateContractDoesNotAcceptUsername() {
        assertThat(java.util.Arrays.stream(UpdateManagedSocialIdentityRequest.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName))
                .containsExactly("displayName", "bio");
    }

    @Test
    void assigningCollaboratorRoleCreatesManagedIdentityAutomatically() {
        AdminSocialIdentityRepository identities = mock(AdminSocialIdentityRepository.class);
        UserRepository users = mock(UserRepository.class);
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        UserProfileValidationSupport validation = mock(UserProfileValidationSupport.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        User admin = user(15L, "collaborator@example.com");
        User actor = user(1L, "master@example.com");

        when(identities.findByAdminId(15L)).thenReturn(Optional.empty());
        UserProfile adminProfile = new UserProfile(admin);
        adminProfile.setDisplayName("Ban Truyền thông");
        when(profiles.findById(15L)).thenReturn(Optional.of(adminProfile));
        when(validation.normalizeAndValidateUsername("identity_f")).thenReturn("identity_f");
        when(profiles.existsByUsername("identity_f")).thenReturn(false);
        when(validation.normalizeAndValidateDisplayName("Ban Truyền thông")).thenReturn("Ban Truyền thông");
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User managed = invocation.getArgument(0);
            ReflectionTestUtils.setField(managed, "id", 1050L);
            return managed;
        });

        service(identities, users, profiles, validation, actions).activateOrCreateForRole(admin, actor);

        verify(profiles).saveAndFlush(any(UserProfile.class));
        verify(profiles).saveAndFlush(org.mockito.ArgumentMatchers.argThat(profile ->
                profile.getProfileCompletedAt() != null
                        && profile.getDateOfBirth() == null
                        && profile.isCompleted()));
        verify(identities).saveAndFlush(any(AdminSocialIdentity.class));
        verify(actions).save(any());
        verify(users).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
                user -> user.getAccountType() == UserAccountType.MANAGED && user.getPasswordHash() == null));
    }

    @Test
    void revokingAndReassigningRoleReuseTheSameIdentity() {
        AdminSocialIdentityRepository identities = mock(AdminSocialIdentityRepository.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        AdminSocialIdentity identity = mock(AdminSocialIdentity.class);
        User managed = user(1050L, null);
        User admin = user(15L, "collaborator@example.com");
        User actor = user(1L, "master@example.com");
        when(identity.getSocialUser()).thenReturn(managed);
        when(identity.getStatus()).thenReturn(ManagedSocialIdentityStatus.ACTIVE)
                .thenReturn(ManagedSocialIdentityStatus.DISABLED);
        when(identities.findByAdminId(15L)).thenReturn(Optional.of(identity));
        ManagedSocialIdentityService service = service(identities, mock(UserRepository.class),
                mock(UserProfileRepository.class), mock(UserProfileValidationSupport.class), actions);

        service.disableForRoleRevocation(15L, actor);
        service.activateOrCreateForRole(admin, actor);

        verify(identity).setStatus(ManagedSocialIdentityStatus.DISABLED);
        verify(identity).setStatus(ManagedSocialIdentityStatus.ACTIVE);
        verify(actions, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void collaboratorCanUpdateOnlyThePersistedPublicProfileOfItsManagedIdentity() {
        AdminSocialIdentityRepository identities = mock(AdminSocialIdentityRepository.class);
        UserRepository users = mock(UserRepository.class);
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        UserProfileValidationSupport validation = mock(UserProfileValidationSupport.class);
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        User managed = user(1050L, null);
        managed.setAccountType(UserAccountType.MANAGED);
        AdminSocialIdentity identity = mock(AdminSocialIdentity.class);
        UserProfile profile = new UserProfile(managed);
        profile.setUsername("identity_f");
        profile.setDisplayName("Tên cũ");

        when(currentUser.getCurrentUserId()).thenReturn(15L);
        when(identities.findByAdminId(15L)).thenReturn(Optional.of(identity));
        when(identity.getStatus()).thenReturn(ManagedSocialIdentityStatus.ACTIVE);
        when(identity.getSocialUser()).thenReturn(managed);
        when(profiles.findByIdForUpdate(1050L)).thenReturn(Optional.of(profile));
        when(profiles.findById(1050L)).thenReturn(Optional.of(profile));
        when(validation.normalizeAndValidateDisplayName("Ban Truyền thông")).thenReturn("Ban Truyền thông");
        when(validation.normalizeAndValidateBio("Tin tức sinh viên")).thenReturn("Tin tức sinh viên");

        ManagedSocialIdentityService service = new ManagedSocialIdentityService(identities,
                new CollaboratorSocialIdentityResolver(identities), users, profiles,
                mock(AdminRoleAssignmentRepository.class), validation, currentUser,
                mock(AdminActionRepository.class), Clock.systemUTC());

        ManagedSocialIdentityResponse response = service.updateCurrent(new UpdateManagedSocialIdentityRequest(
                "Ban Truyền thông", "Tin tức sinh viên"));

        assertThat(response.username()).isEqualTo("identity_f");
        assertThat(response.displayName()).isEqualTo("Ban Truyền thông");
        assertThat(response.avatarUrl()).isNull();
        assertThat(response.bio()).isEqualTo("Tin tức sinh viên");
        assertThat(response.managed()).isTrue();
        verify(validation, never()).normalizeAndValidateUsername(any());
        verify(profiles, never()).existsByUsername(any());
    }

    private ManagedSocialIdentityService service(AdminSocialIdentityRepository identities,
                                                  UserRepository users,
                                                  UserProfileRepository profiles,
                                                  UserProfileValidationSupport validation,
                                                  AdminActionRepository actions) {
        return new ManagedSocialIdentityService(identities, new CollaboratorSocialIdentityResolver(identities),
                users, profiles, mock(AdminRoleAssignmentRepository.class), validation,
                mock(CurrentUserProvider.class), actions, Clock.systemUTC());
    }

    private User user(Long id, String email) {
        User user = new User(email, email == null ? null : "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
