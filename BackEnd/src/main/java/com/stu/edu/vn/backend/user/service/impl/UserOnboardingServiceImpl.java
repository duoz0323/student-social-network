package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.dto.response.OnboardingStatusResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserOnboardingService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý nghiệp vụ onboarding, đảm bảo profile_completed_at chỉ được set một lần.
 */
@Service
public class UserOnboardingServiceImpl implements UserOnboardingService {

    private static final String NEXT_STEP_ONBOARDING_PROFILE = "ONBOARDING_PROFILE";
    private static final String NEXT_STEP_FEED = "FEED";

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileValidationSupport validationSupport;
    private final Clock clock;

    public UserOnboardingServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserProfileValidationSupport validationSupport,
            Clock clock
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.validationSupport = validationSupport;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingStatusResponse getMyOnboardingStatus() {
        Long userId = currentUserProvider.getCurrentUserId();
        ensureActiveUser(userId);
        UserProfile profile = findProfile(userId);
        return toOnboardingStatusResponse(profile);
    }

    @Override
    @Transactional
    public CompleteOnboardingResponse completeOnboarding(CompleteOnboardingRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Long userId = currentUserProvider.getCurrentUserId();
        ensureActiveUser(userId);
        UserProfile profile = findProfile(userId);
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

        return new CompleteOnboardingResponse(
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getDateOfBirth(),
                profile.getBio(),
                true,
                NEXT_STEP_FEED
        );
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

    private OnboardingStatusResponse toOnboardingStatusResponse(UserProfile profile) {
        boolean profileCompleted = profile.getProfileCompletedAt() != null;
        return new OnboardingStatusResponse(
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getDateOfBirth(),
                profile.getBio(),
                profileCompleted,
                profileCompleted ? NEXT_STEP_FEED : NEXT_STEP_ONBOARDING_PROFILE
        );
    }
}
