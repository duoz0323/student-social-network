package com.stu.edu.vn.backend.follow.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.follow.dto.response.FollowStatusResponse;
import com.stu.edu.vn.backend.follow.dto.response.FollowUserResponse;
import com.stu.edu.vn.backend.follow.entity.Follow;
import com.stu.edu.vn.backend.follow.mapper.FollowMapper;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.follow.repository.FollowUserProjection;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class FollowServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final FollowRepository followRepository = org.mockito.Mockito.mock(FollowRepository.class);
    private final FollowMapper followMapper = new FollowMapper();

    private FollowServiceImpl followService;

    @BeforeEach
    void setUp() {
        followService = new FollowServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                followRepository,
                followMapper
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfile(10L)));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L, UserStatus.ACTIVE)));
    }

    @Test
    void followUsesCurrentUserAndPersistsCompositeRelation() {
        FollowStatusResponse response = followService.followUser(20L);

        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        verify(currentUserProvider).getCurrentUserId();
        verify(followRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId().getFollowerId()).isEqualTo(10L);
        assertThat(captor.getValue().getId().getFollowingId()).isEqualTo(20L);
        assertThat(response).isEqualTo(new FollowStatusResponse(20L, true));
    }

    @Test
    void followRejectsSelfAndDuplicateRelation() {
        assertBusinessError(() -> followService.followUser(10L), ErrorCode.FOLLOW_SELF_FORBIDDEN);
        verify(followRepository, never()).saveAndFlush(any());

        when(followRepository.existsByIdFollowerIdAndIdFollowingId(10L, 20L)).thenReturn(true);
        assertBusinessError(() -> followService.followUser(20L), ErrorCode.FOLLOW_ALREADY_EXISTS);
    }

    @Test
    void followConvertsPrimaryKeyRaceToBusinessError() {
        doThrow(new DataIntegrityViolationException("pk_follows"))
                .when(followRepository).saveAndFlush(any());

        assertBusinessError(() -> followService.followUser(20L), ErrorCode.FOLLOW_ALREADY_EXISTS);
    }

    @Test
    void followRejectsBlockedTargetAsNotFound() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L, UserStatus.BLOCKED)));

        assertBusinessError(() -> followService.followUser(20L), ErrorCode.USER_NOT_FOUND);
        verify(followRepository, never()).existsByIdFollowerIdAndIdFollowingId(any(), any());
    }

    @Test
    void socialFeaturesRejectBlockedOrIncompleteCurrentUser() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.BLOCKED)));
        assertBusinessError(() -> followService.followUser(20L), ErrorCode.USER_BLOCKED);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        UserProfile incompleteProfile = completedProfile(10L);
        incompleteProfile.setProfileCompletedAt(null);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(incompleteProfile));
        assertBusinessError(() -> followService.getFollowers(20L), ErrorCode.PROFILE_NOT_COMPLETED);
    }

    @Test
    void unfollowDirectlyDeletesWithoutLoadingTargetStatus() {
        when(followRepository.deleteFollow(10L, 20L)).thenReturn(1);

        FollowStatusResponse response = followService.unfollowUser(20L);

        verify(followRepository).deleteFollow(10L, 20L);
        verify(userRepository, never()).findById(20L);
        assertThat(response).isEqualTo(new FollowStatusResponse(20L, false));
    }

    @Test
    void unfollowReturnsNotFoundWhenDirectDeleteAffectsNoRow() {
        when(followRepository.deleteFollow(10L, 20L)).thenReturn(0);

        assertBusinessError(() -> followService.unfollowUser(20L), ErrorCode.FOLLOW_NOT_FOUND);
    }

    @Test
    void followersAreMappedInRepositoryOrderWithoutPerUserExistsCalls() {
        FollowUserProjection first = projection(30L, Boolean.TRUE, LocalDateTime.of(2026, 7, 12, 11, 0));
        FollowUserProjection second = projection(21L, Boolean.FALSE, LocalDateTime.of(2026, 7, 12, 10, 0));
        when(followRepository.findActiveFollowers(20L, 10L)).thenReturn(List.of(first, second));

        List<FollowUserResponse> response = followService.getFollowers(20L);

        assertThat(response).extracting(FollowUserResponse::userId).containsExactly(30L, 21L);
        assertThat(response.get(0).followedByCurrentUser()).isTrue();
        assertThat(response.get(1).followedByCurrentUser()).isFalse();
        verify(followRepository).findActiveFollowers(20L, 10L);
        verify(followRepository, never()).existsByIdFollowerIdAndIdFollowingId(any(), any());
    }

    @Test
    void followingReturnsEmptyArrayWhenRepositoryHasNoRows() {
        when(followRepository.findActiveFollowing(20L, 10L)).thenReturn(List.of());

        assertThat(followService.getFollowing(20L)).isEmpty();
        verify(followRepository).findActiveFollowing(20L, 10L);
    }

    private void assertBusinessError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode code) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(code);
    }

    private User user(Long id, UserStatus status) {
        User user = new User("student" + id + "@example.com", null, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setStatus(status);
        return user;
    }

    private UserProfile completedProfile(Long userId) {
        UserProfile profile = new UserProfile(user(userId, UserStatus.ACTIVE));
        ReflectionTestUtils.setField(profile, "userId", userId);
        profile.setDisplayName("Nguyen Van A");
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 12, 9, 0));
        return profile;
    }

    private FollowUserProjection projection(Long userId, Boolean followed, LocalDateTime followedAt) {
        return new FollowUserProjection() {
            public Long getUserId() { return userId; }
            public String getDisplayName() { return "User " + userId; }
            public String getAvatarUrl() { return null; }
            public String getBio() { return null; }
            public LocalDateTime getFollowedAt() { return followedAt; }
            public Boolean getFollowedByCurrentUser() { return followed; }
        };
    }
}
