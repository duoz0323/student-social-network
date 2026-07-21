package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.dto.response.OnboardingStatusResponse;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.mapper.UserProfileMapper;
import com.stu.edu.vn.backend.user.service.UserOnboardingService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý nghiệp vụ onboarding, đảm bảo profile_completed_at chỉ được set một lần.
 */
@Service
@RequiredArgsConstructor
public class UserOnboardingServiceImpl implements UserOnboardingService {

    private static final String NEXT_STEP_ONBOARDING_PROFILE = "ONBOARDING_PROFILE";
    private static final String NEXT_STEP_FEED = "FEED";

    private final CurrentUserProfileProvider currentUserProfileProvider;
    private final UserProfileValidationSupport validationSupport;
    private final UserProfileMapper userProfileMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public OnboardingStatusResponse getMyOnboardingStatus() {
        UserProfile profile = currentUserProfileProvider.getCurrentProfile();
        return toOnboardingStatusResponse(profile);
    }

    @Override
    @Transactional
    public CompleteOnboardingResponse completeOnboarding(CompleteOnboardingRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        UserProfile profile = currentUserProfileProvider.getCurrentProfileForUpdate();
        if (profile.getProfileCompletedAt() != null) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_COMPLETED);
        }

        String displayName = validationSupport.normalizeAndValidateDisplayName(request.displayName());
        LocalDate dateOfBirth = validationSupport.validateDateOfBirth(request.dateOfBirth());
        String bio = validationSupport.normalizeAndValidateBio(request.bio());

        profile.setDisplayName(displayName);
        profile.setDateOfBirth(dateOfBirth);
        profile.setBio(bio);
        profile.setProfileCompletedAt(LocalDateTime.now(clock));

        return userProfileMapper.toCompleteOnboardingResponse(profile, NEXT_STEP_FEED);
    }

    private OnboardingStatusResponse toOnboardingStatusResponse(UserProfile profile) {
        boolean profileCompleted = profile.getProfileCompletedAt() != null;
        String nextStep = profileCompleted ? NEXT_STEP_FEED : NEXT_STEP_ONBOARDING_PROFILE;
        return userProfileMapper.toOnboardingStatusResponse(profile, nextStep);
    }
}
