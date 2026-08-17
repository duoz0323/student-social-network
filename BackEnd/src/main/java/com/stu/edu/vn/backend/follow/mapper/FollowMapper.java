package com.stu.edu.vn.backend.follow.mapper;

import com.stu.edu.vn.backend.follow.dto.response.FollowUserResponse;
import com.stu.edu.vn.backend.follow.repository.FollowUserProjection;
import org.springframework.stereotype.Component;
import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import java.util.List;

/**
 * Mapper chuyển projection sang DTO công khai và xử lý an toàn giá trị Boolean nullable từ MySQL.
 */
@Component
public class FollowMapper {

    public FollowUserResponse toResponse(FollowUserProjection projection) {
        return toResponse(projection, List.of());
    }

    public FollowUserResponse toResponse(FollowUserProjection projection, List<PublicUserBadge> badges) {
        return new FollowUserResponse(
                projection.getUserId(),
                projection.getDisplayName(),
                projection.getAvatarUrl(),
                projection.getBio(),
                projection.getFollowedAt(),
                Boolean.TRUE.equals(projection.getFollowedByCurrentUser()),
                badges
        );
    }
}
