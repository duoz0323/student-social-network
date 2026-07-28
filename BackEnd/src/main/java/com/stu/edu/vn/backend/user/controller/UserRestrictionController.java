package com.stu.edu.vn.backend.user.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.user.dto.response.RestrictedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserRestrictionStatusResponse;
import com.stu.edu.vn.backend.user.service.UserRestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API Restrict; người thực hiện luôn được lấy từ JWT tại Service. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestrictionController {
    private final UserRestrictionService restrictionService;

    @PostMapping("/{targetUserId}/restriction")
    public ResponseEntity<ApiResponse<UserRestrictionStatusResponse>> restrict(
            @PathVariable Long targetUserId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hạn chế người dùng thành công", restrictionService.restrict(targetUserId)));
    }

    @DeleteMapping("/{targetUserId}/restriction")
    public ResponseEntity<ApiResponse<UserRestrictionStatusResponse>> unrestrict(
            @PathVariable Long targetUserId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bỏ hạn chế người dùng thành công", restrictionService.unrestrict(targetUserId)));
    }

    @GetMapping("/me/restricted-users")
    public ResponseEntity<ApiResponse<PageResponse<RestrictedUserResponse>>> getMyRestrictedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản đã hạn chế thành công",
                restrictionService.getMyRestrictedUsers(page, size)));
    }
}
