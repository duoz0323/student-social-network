package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.user.dto.response.BlockedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserBlockStatusResponse;

public interface UserBlockService {
    UserBlockStatusResponse block(Long targetUserId);
    UserBlockStatusResponse unblock(Long targetUserId);
    PageResponse<BlockedUserResponse> getMyBlockedUsers(int page, int size);
}
