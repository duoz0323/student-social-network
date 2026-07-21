package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.mapper.UserProfileMapper;
import com.stu.edu.vn.backend.user.service.UserProfileService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý cập nhật hồ sơ sau onboarding, không thay đổi profile_completed_at.
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final CurrentUserProfileProvider currentUserProfileProvider;
    private final UserProfileValidationSupport validationSupport;
    private final UserProfileMapper userProfileMapper;

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        UserProfile profile = currentUserProfileProvider.getCurrentProfileForUpdate();
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }

        String displayName = validationSupport.normalizeAndValidateDisplayName(request.displayName());
        LocalDate dateOfBirth = validationSupport.validateDateOfBirth(request.dateOfBirth());
        String bio = validationSupport.normalizeAndValidateBio(request.bio());

        profile.setDisplayName(displayName);
        profile.setDateOfBirth(dateOfBirth);
        profile.setBio(bio);

        return userProfileMapper.toUserProfileResponse(profile);
    }
}
