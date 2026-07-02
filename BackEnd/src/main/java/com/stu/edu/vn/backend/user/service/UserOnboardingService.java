package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.dto.response.OnboardingStatusResponse;

/**
 * Service quản lý luồng hoàn tất hồ sơ ban đầu của người dùng hiện tại.
 */
public interface UserOnboardingService {

    OnboardingStatusResponse getMyOnboardingStatus();

    CompleteOnboardingResponse completeOnboarding(CompleteOnboardingRequest request);
}
