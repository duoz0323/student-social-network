package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.academic.repository.FacultyRepository;
import com.stu.edu.vn.backend.academic.repository.InterestCategoryRepository;
import com.stu.edu.vn.backend.academic.repository.MajorRepository;
import com.stu.edu.vn.backend.academic.repository.SchoolRepository;
import com.stu.edu.vn.backend.academic.entity.School;
import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.request.AcademicProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.dto.response.UserProfileViewResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import com.stu.edu.vn.backend.user.mapper.UserProfileMapper;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRestrictionRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserProfileServiceImplTest {

    private final CurrentUserProfileProvider currentUserProfileProvider =
            org.mockito.Mockito.mock(CurrentUserProfileProvider.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final FollowRepository followRepository = org.mockito.Mockito.mock(FollowRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-18T10:00:00Z"), ZoneOffset.UTC);
    private final UserRelationshipPolicyService relationshipPolicyService =
            org.mockito.Mockito.mock(UserRelationshipPolicyService.class);
    private final UserRestrictionRepository userRestrictionRepository =
            org.mockito.Mockito.mock(UserRestrictionRepository.class);
    private final SchoolRepository schoolRepository = org.mockito.Mockito.mock(SchoolRepository.class);
    private final FacultyRepository facultyRepository = org.mockito.Mockito.mock(FacultyRepository.class);
    private final MajorRepository majorRepository = org.mockito.Mockito.mock(MajorRepository.class);
    private final InterestCategoryRepository interestRepository =
            org.mockito.Mockito.mock(InterestCategoryRepository.class);
    private final com.stu.edu.vn.backend.user.service.PublicUserBadgeService badgeService =
            org.mockito.Mockito.mock(com.stu.edu.vn.backend.user.service.PublicUserBadgeService.class);

    private UserProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        UserProfileMapper mapper = Mappers.getMapper(UserProfileMapper.class);
        service = new UserProfileServiceImpl(
                currentUserProfileProvider,
                currentUserProvider,
                userProfileRepository,
                followRepository,
                new UserProfileValidationSupport(clock),
                mapper,
                relationshipPolicyService,
                userRestrictionRepository,
                new AcademicProfileValidationSupport(
                        schoolRepository, facultyRepository, majorRepository, interestRepository, clock),
                badgeService
        );
        when(badgeService.getBadges(org.mockito.ArgumentMatchers.anyLong())).thenReturn(List.of());
    }

    @Test
    void getMyProfileUsesJwtUserAndReturnsRealProfileStatistics() {
        User user = org.mockito.Mockito.mock(User.class);
        UserProfile profile = org.mockito.Mockito.mock(UserProfile.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(profile.getUser()).thenReturn(user);
        when(user.getRole()).thenReturn(UserRole.USER);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(profile.getUserId()).thenReturn(10L);
        when(profile.getUsername()).thenReturn("nguyenvana");
        when(profile.getDisplayName()).thenReturn("Nguyễn Văn A");
        when(profile.getProfileCompletedAt()).thenReturn(LocalDateTime.now(clock));
        when(profile.isCompleted()).thenReturn(true);
        when(followRepository.countByIdFollowingId(10L)).thenReturn(4L);
        when(followRepository.countByIdFollowerId(10L)).thenReturn(3L);

        UserProfileViewResponse response = service.getMyProfile();

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.displayName()).isEqualTo("Nguyễn Văn A");
        assertThat(response.followerCount()).isEqualTo(4L);
        assertThat(response.followingCount()).isEqualTo(3L);
        assertThat(response.followedByCurrentUser()).isFalse();
    }

    @Test
    void getPublicProfileRejectsBlockedTargetWithoutLeakingStatus() {
        User user = org.mockito.Mockito.mock(User.class);
        UserProfile profile = org.mockito.Mockito.mock(UserProfile.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userProfileRepository.findById(20L)).thenReturn(Optional.of(profile));
        when(profile.getUser()).thenReturn(user);
        when(user.getRole()).thenReturn(UserRole.USER);
        when(user.getStatus()).thenReturn(UserStatus.BLOCKED);

        assertThatThrownBy(() -> service.getPublicProfile(20L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
    }

    @Test
    void managedPublicProfileUsesPersistedFieldsBadgeAndDisablesMessaging() {
        User managed = org.mockito.Mockito.mock(User.class);
        UserProfile profile = org.mockito.Mockito.mock(UserProfile.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userProfileRepository.findById(20L)).thenReturn(Optional.of(profile));
        when(profile.getUser()).thenReturn(managed);
        when(managed.getRole()).thenReturn(UserRole.USER);
        when(managed.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(managed.getAccountType()).thenReturn(UserAccountType.MANAGED);
        when(profile.getUserId()).thenReturn(20L);
        when(profile.getUsername()).thenReturn("ban_truyen_thong");
        when(profile.getDisplayName()).thenReturn("Ban Truyền thông");
        when(profile.getAvatarUrl()).thenReturn("https://cdn.example/avatar.png");
        when(profile.getBio()).thenReturn("Tin tức sinh viên");
        when(profile.isCompleted()).thenReturn(true);
        when(badgeService.getBadges(20L)).thenReturn(List.of(PublicUserBadge.COLLABORATOR));

        UserProfileViewResponse response = service.getPublicProfile(20L);

        assertThat(response.username()).isEqualTo("ban_truyen_thong");
        assertThat(response.displayName()).isEqualTo("Ban Truyền thông");
        assertThat(response.avatarUrl()).isEqualTo("https://cdn.example/avatar.png");
        assertThat(response.bio()).isEqualTo("Tin tức sinh viên");
        assertThat(response.badges()).containsExactly(PublicUserBadge.COLLABORATOR);
        assertThat(response.canFollow()).isTrue();
        assertThat(response.canMessage()).isFalse();
    }

    @Test
    void normalPublicProfileKeepsMessagingCapabilityAndHasNoCollaboratorBadge() {
        User normal = org.mockito.Mockito.mock(User.class);
        UserProfile profile = org.mockito.Mockito.mock(UserProfile.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userProfileRepository.findById(20L)).thenReturn(Optional.of(profile));
        when(profile.getUser()).thenReturn(normal);
        when(normal.getRole()).thenReturn(UserRole.USER);
        when(normal.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(normal.getAccountType()).thenReturn(UserAccountType.NORMAL);
        when(profile.getUserId()).thenReturn(20L);
        when(profile.getUsername()).thenReturn("student_normal");
        when(profile.getDisplayName()).thenReturn("Sinh viên thường");
        when(profile.isCompleted()).thenReturn(true);
        when(followRepository.existsMutualFollow(10L, 20L)).thenReturn(true);

        UserProfileViewResponse response = service.getPublicProfile(20L);

        assertThat(response.badges()).isEmpty();
        assertThat(response.canMessage()).isTrue();
    }

    @Test
    void normalPublicProfileRequiresMutualFollowForMessagingCapability() {
        User normal = org.mockito.Mockito.mock(User.class);
        UserProfile profile = org.mockito.Mockito.mock(UserProfile.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userProfileRepository.findById(20L)).thenReturn(Optional.of(profile));
        when(profile.getUser()).thenReturn(normal);
        when(normal.getRole()).thenReturn(UserRole.USER);
        when(normal.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(normal.getAccountType()).thenReturn(UserAccountType.NORMAL);
        when(profile.getUserId()).thenReturn(20L);
        when(profile.isCompleted()).thenReturn(true);
        when(followRepository.existsMutualFollow(10L, 20L)).thenReturn(false);

        assertThat(service.getPublicProfile(20L).canMessage()).isFalse();
    }

    @Test
    void publicProfileDoesNotExposeAdminAccount() {
        User admin = org.mockito.Mockito.mock(User.class);
        UserProfile profile = org.mockito.Mockito.mock(UserProfile.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userProfileRepository.findById(20L)).thenReturn(Optional.of(profile));
        when(profile.getUser()).thenReturn(admin);
        when(admin.getRole()).thenReturn(UserRole.ADMIN);
        when(admin.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(profile.isCompleted()).thenReturn(true);

        assertThatThrownBy(() -> service.getPublicProfile(20L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
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
        profile.setUsername("nguyenvanb");
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

    @Test
    void updateAcademicProfileAndInterestsSucceedsWithoutChangingCompletion() {
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        LocalDateTime completedAt = LocalDateTime.now(clock).minusDays(1);
        profile.setUsername("academic_student");
        profile.setProfileCompletedAt(completedAt);
        when(currentUserProfileProvider.getCurrentProfileForUpdate()).thenReturn(profile);

        School school = org.mockito.Mockito.mock(School.class);
        when(school.getId()).thenReturn(1L);
        when(school.getName()).thenReturn("Trường Đại học Công Nghệ Sài Gòn");
        when(school.getShortName()).thenReturn("STU");
        InterestCategory interest = org.mockito.Mockito.mock(InterestCategory.class);
        when(interest.getId()).thenReturn(2L);
        when(interest.getName()).thenReturn("Lập trình");
        when(schoolRepository.findByIdAndStatus(1L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(school));
        when(interestRepository.findAllByIdInAndStatus(
                org.mockito.ArgumentMatchers.anyCollection(),
                org.mockito.ArgumentMatchers.eq(AcademicStatus.ACTIVE)
        )).thenReturn(List.of(interest));

        UserProfileResponse response = service.updateMyProfile(new UpdateUserProfileRequest(
                "Sinh viên STU",
                LocalDate.of(2000, 1, 1),
                null,
                new AcademicProfileRequest(1L, null, null, 2022),
                List.of(2L)
        ));

        assertThat(response.school().shortName()).isEqualTo("STU");
        assertThat(response.entryYear()).isEqualTo(2022);
        assertThat(response.interests()).extracting("id").containsExactly(2L);
        assertThat(profile.getProfileCompletedAt()).isEqualTo(completedAt);
    }
}
