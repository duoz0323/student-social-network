package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserAvatarService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UserOnboardingServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final UserAvatarService userAvatarService = org.mockito.Mockito.mock(UserAvatarService.class);
    private final UserProfileValidationSupport validationSupport = new UserProfileValidationSupport(CLOCK);

    private UserOnboardingServiceImpl userOnboardingService;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        userOnboardingService = new UserOnboardingServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                userAvatarService,
                validationSupport,
                CLOCK
        );
        User user = new User("student@example.com", null, "hash");
        profile = new UserProfile(user);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
    }

    @Test
    void completeOnboardingWithoutAvatarKeepsOldBehavior() {
        CompleteOnboardingResponse response = userOnboardingService.completeOnboarding(validRequest(), null);

        assertThat(response.displayName()).isEqualTo("Nguyen Van A");
        assertThat(response.avatarUrl()).isNull();
        assertThat(response.profileCompleted()).isTrue();
        assertThat(profile.getProfileCompletedAt()).isNotNull();
        verifyNoInteractions(userAvatarService);
    }

    @Test
    void completeOnboardingWithAvatarReusesAvatarService() {
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[]{1});
        doAnswer(invocation -> {
            // Mô phỏng UserAvatarService đã upload và lưu URL/publicId vào cùng hồ sơ người dùng.
            profile.setAvatarUrl("https://cdn.example/avatar.png");
            profile.setAvatarPublicId("avatar-public-id");
            return null;
        }).when(userAvatarService).uploadMyAvatar(avatar);

        CompleteOnboardingResponse response = userOnboardingService.completeOnboarding(validRequest(), avatar);

        verify(userAvatarService).uploadMyAvatar(avatar);
        assertThat(response.avatarUrl()).isEqualTo("https://cdn.example/avatar.png");
        assertThat(profile.getAvatarPublicId()).isEqualTo("avatar-public-id");
        assertThat(profile.getProfileCompletedAt()).isNotNull();
    }

    @Test
    void completeOnboardingKeepsDisplayNameValidationBeforeAvatarUpload() {
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[]{1});
        CompleteOnboardingRequest request = new CompleteOnboardingRequest("A", LocalDate.of(2000, 1, 1), "Bio");

        assertThatThrownBy(() -> userOnboardingService.completeOnboarding(request, avatar))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_DISPLAY_NAME));

        verify(userAvatarService, never()).uploadMyAvatar(avatar);
    }

    @Test
    void completeOnboardingPropagatesAvatarValidationError() {
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.txt", "text/plain", new byte[]{1});
        doThrow(new BusinessException(ErrorCode.AVATAR_FILE_TYPE_NOT_ALLOWED))
                .when(userAvatarService).uploadMyAvatar(avatar);

        assertThatThrownBy(() -> userOnboardingService.completeOnboarding(validRequest(), avatar))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AVATAR_FILE_TYPE_NOT_ALLOWED));
    }

    @Test
    void completeOnboardingRequiresAuthenticatedUser() {
        when(currentUserProvider.getCurrentUserId()).thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

        assertThatThrownBy(() -> userOnboardingService.completeOnboarding(validRequest(), null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    private CompleteOnboardingRequest validRequest() {
        return new CompleteOnboardingRequest(" Nguyen Van A ", LocalDate.of(2000, 1, 1), " Sinh vien ");
    }
}
