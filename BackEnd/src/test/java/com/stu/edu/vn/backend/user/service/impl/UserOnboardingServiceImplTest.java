package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.dto.response.OnboardingStatusResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.mapper.UserProfileMapper;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserOnboardingServiceImplTest {

    private final CurrentUserProfileProvider currentUserProfileProvider =
            org.mockito.Mockito.mock(CurrentUserProfileProvider.class);
    private final UserProfileRepository userProfileRepository =
            org.mockito.Mockito.mock(UserProfileRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-18T10:00:00Z"), ZoneOffset.UTC);
    private final UserProfileMapper mapper = Mappers.getMapper(UserProfileMapper.class);

    private UserOnboardingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserOnboardingServiceImpl(
                currentUserProfileProvider,
                new UserProfileValidationSupport(clock),
                userProfileRepository,
                mapper,
                clock
        );
    }

    @Test
    void completeOnboardingStoresRequiredFieldsAndCompletionTime() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        when(currentUserProfileProvider.getCurrentProfileForUpdate()).thenReturn(profile);

        CompleteOnboardingResponse response = service.completeOnboarding(
                new CompleteOnboardingRequest(
                        "  DuOz_03  ", "  Nguyễn Văn A  ", LocalDate.of(2008, 7, 18), "  Sinh viên  ")
        );

        verify(currentUserProfileProvider).getCurrentProfileForUpdate();
        assertThat(profile.getUsername()).isEqualTo("duoz_03");
        assertThat(profile.getDisplayName()).isEqualTo("Nguyễn Văn A");
        assertThat(profile.getDateOfBirth()).isEqualTo(LocalDate.of(2008, 7, 18));
        assertThat(profile.getBio()).isEqualTo("Sinh viên");
        assertThat(profile.getProfileCompletedAt()).isEqualTo(LocalDateTime.now(clock));
        assertThat(response.profileCompleted()).isTrue();
        assertThat(response.nextStep()).isEqualTo("FEED");
    }

    @Test
    void completeOnboardingRejectsAlreadyCompletedProfile() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        profile.setUsername("student_01");
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        when(currentUserProfileProvider.getCurrentProfileForUpdate()).thenReturn(profile);

        assertThatThrownBy(() -> service.completeOnboarding(
                new CompleteOnboardingRequest("student_02", "Nguyễn Văn A", LocalDate.of(2000, 1, 1), null)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_ALREADY_COMPLETED);
    }

    @Test
    void getStatusReturnsOnboardingStepForIncompleteProfile() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        when(currentUserProfileProvider.getCurrentProfile()).thenReturn(profile);

        OnboardingStatusResponse response = service.getMyOnboardingStatus();

        assertThat(response.profileCompleted()).isFalse();
        assertThat(response.nextStep()).isEqualTo("ONBOARDING_PROFILE");
    }

    @Test
    void getStatusTreatsLegacyTimestampWithoutUsernameAsIncomplete() {
        UserProfile profile = new UserProfile(new User("legacy@example.com", "hash"));
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        when(currentUserProfileProvider.getCurrentProfile()).thenReturn(profile);

        OnboardingStatusResponse response = service.getMyOnboardingStatus();

        assertThat(response.profileCompleted()).isFalse();
        assertThat(response.nextStep()).isEqualTo("ONBOARDING_PROFILE");
    }

    @Test
    void availabilityReturnsNormalizedUsernameAndRepositoryResult() {
        when(userProfileRepository.existsByUsername("duoz_03")).thenReturn(false);

        var response = service.checkUsernameAvailability(" DuOz_03 ");

        assertThat(response.username()).isEqualTo("duoz_03");
        assertThat(response.available()).isTrue();
    }

    @Test
    void completeOnboardingRejectsExistingUsername() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        when(currentUserProfileProvider.getCurrentProfileForUpdate()).thenReturn(profile);
        when(userProfileRepository.existsByUsername("duoz_03")).thenReturn(true);

        assertThatThrownBy(() -> service.completeOnboarding(new CompleteOnboardingRequest(
                "duoz_03", "Nguyễn Văn A", LocalDate.of(2000, 1, 1), null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);
    }

    @Test
    void completeOnboardingMapsUniqueConstraintRaceToBusinessError() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        when(currentUserProfileProvider.getCurrentProfileForUpdate()).thenReturn(profile);
        when(userProfileRepository.saveAndFlush(profile)).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException(
                        "Duplicate entry for key 'uq_user_profiles_username'"));

        assertThatThrownBy(() -> service.completeOnboarding(new CompleteOnboardingRequest(
                "duoz_03", "Nguyễn Văn A", LocalDate.of(2000, 1, 1), null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);
    }
}
