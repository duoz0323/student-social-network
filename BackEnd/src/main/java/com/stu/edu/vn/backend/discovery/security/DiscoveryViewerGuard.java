package com.stu.edu.vn.backend.discovery.security;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Dùng chung điều kiện USER/ACTIVE/onboarding cho mọi nhánh Discovery. */
@Component
@RequiredArgsConstructor
public class DiscoveryViewerGuard {
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileRepository userProfileRepository;

    public CustomUserPrincipal requireEligibleViewer() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.USER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (principal.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        if (!userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(principal.getUserId())) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return principal;
    }
}
