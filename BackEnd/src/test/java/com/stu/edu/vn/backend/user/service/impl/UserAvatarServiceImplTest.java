package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import com.stu.edu.vn.backend.storage.CloudinaryUploadResult;
import com.stu.edu.vn.backend.user.dto.response.AvatarResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

class UserAvatarServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final CloudinaryStorageService cloudinaryStorageService = org.mockito.Mockito.mock(CloudinaryStorageService.class);
    private final UserAvatarFileValidator fileValidator = org.mockito.Mockito.mock(UserAvatarFileValidator.class);
    private final TransactionTemplate transactionTemplate = org.mockito.Mockito.mock(TransactionTemplate.class);

    private UserAvatarServiceImpl userAvatarService;

    @BeforeEach
    void setUp() {
        userAvatarService = new UserAvatarServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                cloudinaryStorageService,
                fileValidator,
                transactionTemplate
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(new User("student@example.com", null, "hash")));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
    }

    @Test
    void uploadMyAvatarStoresNewAvatarAndDeletesOldAvatarAfterDatabaseSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1});
        UserProfile profile = completedProfileWithOldAvatar();
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(cloudinaryStorageService.uploadAvatar(file))
                .thenReturn(new CloudinaryUploadResult("https://cdn.example/new.png", "new-public-id"));
        when(userProfileRepository.saveAndFlush(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvatarResponse response = userAvatarService.uploadMyAvatar(file);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).saveAndFlush(profileCaptor.capture());
        verify(cloudinaryStorageService).deleteImage("old-public-id");
        assertThat(profileCaptor.getValue().getAvatarUrl()).isEqualTo("https://cdn.example/new.png");
        assertThat(profileCaptor.getValue().getAvatarPublicId()).isEqualTo("new-public-id");
        assertThat(response.avatarUrl()).isEqualTo("https://cdn.example/new.png");
        assertThat(response.profileCompleted()).isTrue();
    }

    @Test
    void uploadMyAvatarCleansNewCloudinaryFileWhenDatabaseUpdateFails() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1});
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfileWithOldAvatar()));
        when(cloudinaryStorageService.uploadAvatar(file))
                .thenReturn(new CloudinaryUploadResult("https://cdn.example/new.png", "new-public-id"));
        doThrow(new IllegalStateException("database failed"))
                .when(userProfileRepository).saveAndFlush(any(UserProfile.class));

        assertThatThrownBy(() -> userAvatarService.uploadMyAvatar(file))
                .isInstanceOf(IllegalStateException.class);

        verify(cloudinaryStorageService).deleteImage("new-public-id");
    }

    @Test
    void deleteMyAvatarClearsDatabaseAndDeletesOldAvatar() {
        UserProfile profile = completedProfileWithOldAvatar();
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.saveAndFlush(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvatarResponse response = userAvatarService.deleteMyAvatar();

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).saveAndFlush(profileCaptor.capture());
        verify(cloudinaryStorageService).deleteImage("old-public-id");
        assertThat(profileCaptor.getValue().getAvatarUrl()).isNull();
        assertThat(profileCaptor.getValue().getAvatarPublicId()).isNull();
        assertThat(response.avatarUrl()).isNull();
        assertThat(response.profileCompleted()).isTrue();
    }

    private UserProfile completedProfileWithOldAvatar() {
        User user = new User("student@example.com", null, "hash");
        ReflectionTestUtils.setField(user, "id", 10L);
        UserProfile profile = new UserProfile(user);
        profile.setAvatarUrl("https://cdn.example/old.png");
        profile.setAvatarPublicId("old-public-id");
        profile.setProfileCompletedAt(LocalDateTime.now());
        return profile;
    }
}
