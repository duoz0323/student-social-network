package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.request.AdminBlockUserRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserStatusResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.user.enums.UserStatus;

/**
 * Use case đọc danh sách và chi tiết tài khoản USER dành cho ADMIN.
 */
public interface AdminUserService {

    PageResponse<AdminUserListItemResponse> getUsers(String keyword, UserStatus status, int page, int size);

    AdminUserDetailResponse getUserDetail(Long userId);

    AdminUserStatusResponse blockUser(Long userId, AdminBlockUserRequest request);

    AdminUserStatusResponse unblockUser(Long userId);
}
