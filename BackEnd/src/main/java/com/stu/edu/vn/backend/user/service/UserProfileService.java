package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.user.dto.request.UpdateUserProfileRequest;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;

/**
 * Service quản lý cập nhật hồ sơ sau khi người dùng đã hoàn tất onboarding.
 */
public interface UserProfileService {

    UserProfileResponse updateMyProfile(UpdateUserProfileRequest request);
}
