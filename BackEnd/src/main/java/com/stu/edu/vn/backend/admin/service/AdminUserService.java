package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.request.AdminBlockUserRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminUpdateUserProfileRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserStatusResponse;
import com.stu.edu.vn.backend.admin.enums.AdminAvatarAction;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Use case quản lý danh sách, chi tiết, hồ sơ và trạng thái tài khoản USER dành cho ADMIN.
 */
public interface AdminUserService {

    PageResponse<AdminUserListItemResponse> getUsers(String keyword, UserStatus status, int page, int size);

    AdminUserDetailResponse getUserDetail(Long userId);

    AdminUserDetailResponse updateUserProfile(Long userId, AdminUpdateUserProfileRequest request);

    AdminUserDetailResponse updateUserProfileWithAvatar(
            Long userId,
            AdminUpdateUserProfileRequest request,
            AdminAvatarAction avatarAction,
            MultipartFile avatarFile
    );

    AdminUserStatusResponse blockUser(Long userId, AdminBlockUserRequest request);

    AdminUserStatusResponse unblockUser(Long userId);
}
