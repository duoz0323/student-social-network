package com.stu.edu.vn.backend.admin.mapper;

import com.stu.edu.vn.backend.admin.dto.response.AdminUserDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserStatusResponse;
import com.stu.edu.vn.backend.admin.repository.AdminUserDetailProjection;
import com.stu.edu.vn.backend.admin.repository.AdminUserListProjection;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Chuyển projection sang response DTO để Controller không làm lộ cấu trúc lưu trữ.
 */
@Component
public class AdminUserMapper {

    public AdminUserListItemResponse toListItem(AdminUserListProjection source) {
        return new AdminUserListItemResponse(
                source.getUserId(),
                source.getDisplayName(),
                source.getAvatarUrl(),
                source.getEmail(),
                UserStatus.valueOf(source.getStatus()),
                source.getProfileCompletedAt() != null,
                source.getCreatedAt()
        );
    }

    public AdminUserDetailResponse toDetail(AdminUserDetailProjection source) {
        return new AdminUserDetailResponse(
                source.getUserId(),
                source.getDisplayName(),
                source.getAvatarUrl(),
                source.getBio(),
                source.getEmail(),
                UserStatus.valueOf(source.getStatus()),
                source.getProfileCompletedAt() != null,
                source.getProfileCompletedAt(),
                source.getBlockedAt(),
                source.getBlockedReason(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }

    public AdminUserStatusResponse toStatus(User user) {
        return new AdminUserStatusResponse(
                user.getId(),
                user.getStatus(),
                user.getBlockedAt(),
                user.getBlockedReason(),
                user.getUpdatedAt()
        );
    }
}
