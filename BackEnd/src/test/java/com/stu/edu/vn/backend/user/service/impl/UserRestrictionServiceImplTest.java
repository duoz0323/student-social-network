package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.repository.UserRestrictionRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserRestrictionServiceImplTest {
    private final CurrentUserProvider currentUserProvider =
            org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserRestrictionRepository repository =
            org.mockito.Mockito.mock(UserRestrictionRepository.class);
    private final UserRelationshipPolicyService relationshipPolicy =
            org.mockito.Mockito.mock(UserRelationshipPolicyService.class);
    private UserRestrictionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserRestrictionServiceImpl(
                currentUserProvider, userRepository, repository, relationshipPolicy);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        User currentUser = user(10L);
        User targetUser = user(20L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(20L)).thenReturn(Optional.of(targetUser));
    }

    @Test
    void restrictIsIdempotentAndOnlyCreatesDirectedRelation() {
        assertThat(service.restrict(20L).restrictedByMe()).isTrue();
        verify(repository).insertIfAbsent(10L, 20L);
    }

    @Test
    void cannotRestrictSelf() {
        assertThatThrownBy(() -> service.restrict(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CANNOT_RESTRICT_SELF);
        verify(repository, never()).insertIfAbsent(10L, 10L);
    }

    @Test
    void blockEitherDirectionPreventsRestriction() {
        when(relationshipPolicy.existsBlockEitherDirection(10L, 20L)).thenReturn(true);
        assertThatThrownBy(() -> service.restrict(20L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_RELATIONSHIP_BLOCKED);
        verify(repository, never()).insertIfAbsent(10L, 20L);
    }

    @Test
    void unrestrictIsIdempotentAndDoesNotTouchReverseDirection() {
        assertThat(service.unrestrict(20L).restrictedByMe()).isFalse();
        verify(repository).deleteRestriction(10L, 20L);
        verify(repository, never()).deleteRestriction(20L, 10L);
    }

    private User user(Long id) {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(user.getRole()).thenReturn(UserRole.USER);
        return user;
    }
}
