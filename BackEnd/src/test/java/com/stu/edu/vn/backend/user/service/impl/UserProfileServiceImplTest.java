package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.mapper.UserProfileMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserProfileServiceImplTest {

    private final CurrentUserProfileProvider currentUserProfileProvider =
            org.mockito.Mockito.mock(CurrentUserProfileProvider.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-18T10:00:00Z"), ZoneOffset.UTC);

    private UserProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        UserProfileMapper mapper = Mappers.getMapper(UserProfileMapper.class);
        service = new UserProfileServiceImpl(
                currentUserProfileProvider,
                new UserProfileValidationSupport(clock),
                mapper
        );
    }

    @Test
    void updateProfileRejectsIncompleteProfile() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        when(currentUserProfileProvider.getCurrentProfileForUpdate()).thenReturn(profile);

        assertThatThrownBy(() -> service.updateMyProfile(
                new UpdateUserProfileRequest("Nguyễn Văn A", LocalDate.of(2000, 1, 1), null)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_COMPLETED);
    }

    @Test
    void updateProfileUsesMapperAndKeepsCompletionState() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        profile.setProfileCompletedAt(LocalDateTime.now(clock).minusDays(1));
        when(currentUserProfileProvider.getCurrentProfileForUpdate()).thenReturn(profile);

        UserProfileResponse response = service.updateMyProfile(
                new UpdateUserProfileRequest("  Nguyễn Văn B ", LocalDate.of(2000, 1, 1), "  Bio mới  ")
        );

        assertThat(response.displayName()).isEqualTo("Nguyễn Văn B");
        assertThat(response.bio()).isEqualTo("Bio mới");
        assertThat(response.profileCompleted()).isTrue();
        assertThat(profile.getProfileCompletedAt()).isEqualTo(LocalDateTime.now(clock).minusDays(1));
    }
}
