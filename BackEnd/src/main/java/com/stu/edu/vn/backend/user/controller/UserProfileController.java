package com.stu.edu.vn.backend.user.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.dto.response.UserProfileViewResponse;
import com.stu.edu.vn.backend.user.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller đọc hồ sơ công khai và cập nhật hồ sơ của người dùng hiện tại.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileViewResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy hồ sơ cá nhân thành công",
                userProfileService.getMyProfile()
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileViewResponse>> getPublicProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy hồ sơ người dùng thành công",
                userProfileService.getPublicProfile(userId)
        ));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfileResponse response = userProfileService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", response));
    }
}
