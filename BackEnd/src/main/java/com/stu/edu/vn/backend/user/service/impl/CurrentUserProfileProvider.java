package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cung cấp hồ sơ thuộc người dùng hiện tại và gom kiểm tra trạng thái dùng chung cho các Service /me.
 */
@Component
@RequiredArgsConstructor
class CurrentUserProfileProvider {

    private final CurrentUserProvider currentUserProvider;
    private final UserProfileRepository userProfileRepository;

    UserProfile getCurrentProfile() {
        return findProfile(getCurrentActiveUser().getUserId(), false);
    }

    UserProfile getCurrentProfileForUpdate() {
        return findProfile(getCurrentActiveUser().getUserId(), true);
    }

    Long getCurrentActiveUserId() {
        return getCurrentActiveUser().getUserId();
    }

    private CustomUserPrincipal getCurrentActiveUser() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        return principal;
    }

    private UserProfile findProfile(Long userId, boolean forUpdate) {
        return (forUpdate
                ? userProfileRepository.findByIdForUpdate(userId)
                : userProfileRepository.findById(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
