package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.dto.response.OnboardingStatusResponse;
import com.stu.edu.vn.backend.user.dto.response.UsernameAvailabilityResponse;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.mapper.UserProfileMapper;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.service.UserOnboardingService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public OnboardingStatusResponse getMyOnboardingStatus() {
        UserProfile profile = currentUserProfileProvider.getCurrentProfile();
        return toOnboardingStatusResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse checkUsernameAvailability(String username) {
        String normalizedUsername = validationSupport.normalizeAndValidateUsername(username);
        return new UsernameAvailabilityResponse(
                normalizedUsername,
                !userProfileRepository.existsByUsername(normalizedUsername)
        );
    }

    @Override
    @Transactional
    public CompleteOnboardingResponse completeOnboarding(CompleteOnboardingRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        UserProfile profile = currentUserProfileProvider.getCurrentProfileForUpdate();
        if (profile.isCompleted()) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_COMPLETED);
        }

        String username = validationSupport.normalizeAndValidateUsername(request.username());
        if (userProfileRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        String displayName = validationSupport.normalizeAndValidateDisplayName(request.displayName());
        LocalDate dateOfBirth = validationSupport.validateDateOfBirth(request.dateOfBirth());
        String bio = validationSupport.normalizeAndValidateBio(request.bio());

        profile.setUsername(username);
        profile.setDisplayName(displayName);
        profile.setDateOfBirth(dateOfBirth);
        profile.setBio(bio);
        profile.setProfileCompletedAt(LocalDateTime.now(clock));

        try {
            // Flush trong transaction để unique constraint xử lý race trước khi trả response thành công.
            userProfileRepository.saveAndFlush(profile);
        } catch (DataIntegrityViolationException exception) {
            if (isUsernameUniqueConstraintViolation(exception)) {
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
            throw exception;
        }

        return userProfileMapper.toCompleteOnboardingResponse(profile, NEXT_STEP_FEED);
    }

    private OnboardingStatusResponse toOnboardingStatusResponse(UserProfile profile) {
        boolean profileCompleted = profile.isCompleted();
        String nextStep = profileCompleted ? NEXT_STEP_FEED : NEXT_STEP_ONBOARDING_PROFILE;
        return userProfileMapper.toOnboardingStatusResponse(profile, nextStep);
    }

    private boolean isUsernameUniqueConstraintViolation(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(java.util.Locale.ROOT)
                    .contains("uq_user_profiles_username")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
