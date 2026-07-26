package com.stu.edu.vn.backend.user.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.user.dto.response.BlockedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserBlockStatusResponse;
import com.stu.edu.vn.backend.user.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API quản lý Block; blocker luôn được lấy từ JWT tại tầng Service. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserBlockController {
    private final UserBlockService userBlockService;

    @PutMapping("/{targetUserId}/block")
    public ResponseEntity<ApiResponse<UserBlockStatusResponse>> block(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(ApiResponse.success("Chặn người dùng thành công", userBlockService.block(targetUserId)));
    }

    @DeleteMapping("/{targetUserId}/block")
    public ResponseEntity<ApiResponse<UserBlockStatusResponse>> unblock(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(ApiResponse.success("Bỏ chặn người dùng thành công", userBlockService.unblock(targetUserId)));
    }

    @GetMapping("/me/blocked-users")
    public ResponseEntity<ApiResponse<PageResponse<BlockedUserResponse>>> getMyBlockedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản đã chặn thành công",
                userBlockService.getMyBlockedUsers(page, size)));
    }
}
