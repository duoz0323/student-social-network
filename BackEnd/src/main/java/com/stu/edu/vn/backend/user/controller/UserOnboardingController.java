package com.stu.edu.vn.backend.user.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.user.dto.request.CompleteOnboardingRequest;
import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.dto.response.OnboardingStatusResponse;
import com.stu.edu.vn.backend.user.service.UserOnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller onboarding chỉ nhận request của người dùng hiện tại và ủy quyền nghiệp vụ cho Service.
 */
@RestController
@RequestMapping("/api/v1/users/me/onboarding")
public class UserOnboardingController {

    private final UserOnboardingService userOnboardingService;

    public UserOnboardingController(UserOnboardingService userOnboardingService) {
        this.userOnboardingService = userOnboardingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getMyOnboardingStatus() {
        OnboardingStatusResponse response = userOnboardingService.getMyOnboardingStatus();
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái onboarding thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CompleteOnboardingResponse>> completeOnboarding(
            @RequestBody CompleteOnboardingRequest request
    ) {
        CompleteOnboardingResponse response = userOnboardingService.completeOnboarding(request);
        return ResponseEntity.ok(ApiResponse.success("Hoàn tất hồ sơ ban đầu thành công", response));
    }
}
