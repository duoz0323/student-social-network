package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.user.dto.response.RestrictedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserRestrictionStatusResponse;

public interface UserRestrictionService {
    UserRestrictionStatusResponse restrict(Long targetUserId);
    UserRestrictionStatusResponse unrestrict(Long targetUserId);
    PageResponse<RestrictedUserResponse> getMyRestrictedUsers(int page, int size);
}
