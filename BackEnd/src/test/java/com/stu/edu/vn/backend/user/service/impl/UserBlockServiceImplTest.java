package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.response.UserBlockStatusResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserBlockRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.repository.UserRestrictionRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserBlockServiceImplTest {

    private final CurrentUserProvider currentUserProvider =
            org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserBlockRepository userBlockRepository =
            org.mockito.Mockito.mock(UserBlockRepository.class);
    private final FollowRepository followRepository = org.mockito.Mockito.mock(FollowRepository.class);
    private final UserRestrictionRepository userRestrictionRepository =
            org.mockito.Mockito.mock(UserRestrictionRepository.class);
    private UserBlockServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserBlockServiceImpl(
                currentUserProvider, userRepository, userBlockRepository, followRepository,
                userRestrictionRepository);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, UserStatus.ACTIVE)));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L, UserStatus.ACTIVE)));
    }

    @Test
    void blockRejectsCurrentUserBeforeWritingData() {
        assertThatThrownBy(() -> service.block(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CANNOT_BLOCK_SELF);

        verify(userBlockRepository, never()).insertIfAbsent(10L, 10L);
        verify(followRepository, never()).deleteFollow(10L, 10L);
    }

    @Test
    void blockIsIdempotentAndDeletesFollowInBothDirections() {
        when(userBlockRepository.insertIfAbsent(10L, 20L)).thenReturn(0);

        UserBlockStatusResponse response = service.block(20L);

        assertThat(response).isEqualTo(new UserBlockStatusResponse(20L, true));
        verify(userBlockRepository).insertIfAbsent(10L, 20L);
        verify(followRepository).deleteFollow(10L, 20L);
        verify(followRepository).deleteFollow(20L, 10L);
    }

    @Test
    void unblockIsIdempotentAndDoesNotRestoreFollow() {
        when(userBlockRepository.deleteBlock(10L, 20L)).thenReturn(0);

        UserBlockStatusResponse response = service.unblock(20L);

        assertThat(response).isEqualTo(new UserBlockStatusResponse(20L, false));
        verify(userBlockRepository).deleteBlock(10L, 20L);
        verify(followRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void blockRejectsMissingOrInactiveTargetWithoutCreatingRelation() {
        when(userRepository.findById(20L)).thenReturn(Optional.empty());
        assertBusinessError(() -> service.block(20L), ErrorCode.USER_NOT_FOUND);

        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L, UserStatus.BLOCKED)));
        assertBusinessError(() -> service.block(20L), ErrorCode.USER_NOT_FOUND);
        verify(userBlockRepository, never()).insertIfAbsent(10L, 20L);
    }

    private void assertBusinessError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private User user(Long id, UserStatus status) {
        User user = new User("student" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setStatus(status);
        return user;
    }
}
