package com.stu.edu.vn.backend.discovery.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Kiểm tra guard eligibility dùng chung không nới lỏng USER/ACTIVE/onboarding. */
@ExtendWith(MockitoExtension.class)
class DiscoveryViewerGuardTest {
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserProfileRepository userProfileRepository;
    private DiscoveryViewerGuard guard;

    @BeforeEach
    void setUp() {
        guard = new DiscoveryViewerGuard(currentUserProvider, userProfileRepository);
    }

    @Test
    void acceptsOnlyActiveCompletedUser() {
        CustomUserPrincipal principal = new CustomUserPrincipal(7L, UserRole.USER, UserStatus.ACTIVE);
        when(currentUserProvider.getCurrentUser()).thenReturn(principal);
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(7L)).thenReturn(true);

        assertThat(guard.requireEligibleViewer()).isSameAs(principal);
    }

    @Test
    void rejectsAdminBlockedAndIncompleteViewerWithExistingErrors() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(7L, UserRole.ADMIN, UserStatus.ACTIVE))
                .thenReturn(new CustomUserPrincipal(7L, UserRole.USER, UserStatus.BLOCKED))
                .thenReturn(new CustomUserPrincipal(7L, UserRole.USER, UserStatus.ACTIVE));

        assertError(ErrorCode.FORBIDDEN);
        assertError(ErrorCode.USER_BLOCKED);
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(7L)).thenReturn(false);
        assertError(ErrorCode.PROFILE_NOT_COMPLETED);
    }

    private void assertError(ErrorCode expected) {
        assertThatThrownBy(guard::requireEligibleViewer)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(expected));
    }
}
