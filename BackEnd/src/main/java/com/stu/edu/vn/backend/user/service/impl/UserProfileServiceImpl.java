package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserProfileService;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý cập nhật hồ sơ sau onboarding, không thay đổi profile_completed_at.
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileValidationSupport validationSupport;

    public UserProfileServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserProfileValidationSupport validationSupport
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.validationSupport = validationSupport;
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Long userId = currentUserProvider.getCurrentUserId();
        ensureActiveUser(userId);
        UserProfile profile = findProfile(userId);
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }

        String displayName = validationSupport.normalizeAndValidateDisplayName(request.displayName());
        LocalDate dateOfBirth = validationSupport.validateDateOfBirth(request.dateOfBirth());
        String bio = validationSupport.normalizeAndValidateBio(request.bio());

        profile.setDisplayName(displayName);
        profile.setDateOfBirth(dateOfBirth);
        profile.setBio(bio);

        return toUserProfileResponse(profile);
    }

    private void ensureActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
    }

    private UserProfile findProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private UserProfileResponse toUserProfileResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getDateOfBirth(),
                profile.getBio(),
                profile.getProfileCompletedAt() != null
        );
    }
}
