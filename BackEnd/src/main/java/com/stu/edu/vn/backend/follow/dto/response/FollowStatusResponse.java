package com.stu.edu.vn.backend.follow.dto.response;

/**
 * Response tối giản xác nhận trạng thái Follow mới sau thao tác ghi.
 */
public record FollowStatusResponse(
        Long userId,
        boolean followedByCurrentUser
) {
}
