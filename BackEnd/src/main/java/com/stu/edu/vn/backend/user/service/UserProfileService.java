package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.dto.response.UserProfileViewResponse;

/**
 * Service quản lý cập nhật hồ sơ sau khi người dùng đã hoàn tất onboarding.
 */
public interface UserProfileService {

    UserProfileViewResponse getMyProfile();

    UserProfileViewResponse getPublicProfile(Long userId);

    UserProfileResponse updateMyProfile(UpdateUserProfileRequest request);
}
