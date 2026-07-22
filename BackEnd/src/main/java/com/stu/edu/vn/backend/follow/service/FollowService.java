package com.stu.edu.vn.backend.follow.service;

import com.stu.edu.vn.backend.follow.dto.response.FollowStatusResponse;
import com.stu.edu.vn.backend.follow.dto.response.FollowUserResponse;
import java.util.List;

/**
 * Contract nghiệp vụ Follow/Unfollow và đọc toàn bộ danh sách theo phạm vi MVP.
 */
public interface FollowService {

    FollowStatusResponse followUser(Long userId);

    FollowStatusResponse unfollowUser(Long userId);

    List<FollowUserResponse> getFollowers(Long userId);

    List<FollowUserResponse> getFollowing(Long userId);
}
