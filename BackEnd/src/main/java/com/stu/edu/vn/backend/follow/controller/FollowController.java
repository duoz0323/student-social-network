package com.stu.edu.vn.backend.follow.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.follow.dto.response.FollowStatusResponse;
import com.stu.edu.vn.backend.follow.dto.response.FollowUserResponse;
import com.stu.edu.vn.backend.follow.service.FollowService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller Follow chỉ nhận target userId từ URL; follower luôn được lấy từ SecurityContext tại Service.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> followUser(@PathVariable Long userId) {
        FollowStatusResponse response = followService.followUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Theo dõi người dùng thành công", response));
    }

    @DeleteMapping("/follow")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> unfollowUser(@PathVariable Long userId) {
        FollowStatusResponse response = followService.unfollowUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Bỏ theo dõi người dùng thành công", response));
    }

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<FollowUserResponse>>> getFollowers(@PathVariable Long userId) {
        List<FollowUserResponse> response = followService.getFollowers(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người theo dõi thành công", response));
    }

    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<FollowUserResponse>>> getFollowing(@PathVariable Long userId) {
        List<FollowUserResponse> response = followService.getFollowing(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đang theo dõi thành công", response));
    }
}
