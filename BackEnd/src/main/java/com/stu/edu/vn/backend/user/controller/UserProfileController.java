package com.stu.edu.vn.backend.user.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller hồ sơ cá nhân sau onboarding; không nhận userId từ Client.
 */
@RestController
@RequestMapping("/api/v1/users/me/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfileResponse response = userProfileService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", response));
    }
}
