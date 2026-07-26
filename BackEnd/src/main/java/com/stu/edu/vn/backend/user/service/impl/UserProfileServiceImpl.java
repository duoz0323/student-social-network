package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.dto.response.UserProfileViewResponse;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.mapper.UserProfileMapper;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.service.UserProfileService;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
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
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final UserProfileValidationSupport validationSupport;
    private final UserProfileMapper userProfileMapper;
    private final UserRelationshipPolicyService relationshipPolicyService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileViewResponse getMyProfile() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return getAvailableProfile(currentUserId, currentUserId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileViewResponse getPublicProfile(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }
        return getAvailableProfile(userId, currentUserProvider.getCurrentUserId(), false);
    }

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

    private UserProfileViewResponse getAvailableProfile(
            Long profileUserId,
            Long currentUserId,
            boolean includePrivateFields
    ) {
        if (!profileUserId.equals(currentUserId)
                && relationshipPolicyService.existsBlockEitherDirection(currentUserId, profileUserId)) {
            // Trả 404 để không tiết lộ sự tồn tại hoặc trạng thái Block của tài khoản.
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }
        UserProfile profile = userProfileRepository.findById(profileUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getUser().getStatus() != UserStatus.ACTIVE || profile.getProfileCompletedAt() == null) {
            // Không tiết lộ trạng thái khóa hoặc onboarding của tài khoản qua API hồ sơ công khai.
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }

        return new UserProfileViewResponse(
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                // Ngày sinh chỉ cần cho chủ tài khoản chỉnh sửa, không trả ở hồ sơ người khác.
                includePrivateFields ? profile.getDateOfBirth() : null,
                profile.getBio(),
                followRepository.countByIdFollowingId(profileUserId),
                followRepository.countByIdFollowerId(profileUserId),
                !profileUserId.equals(currentUserId)
                        && followRepository.existsByIdFollowerIdAndIdFollowingId(currentUserId, profileUserId)
        );
    }
}
