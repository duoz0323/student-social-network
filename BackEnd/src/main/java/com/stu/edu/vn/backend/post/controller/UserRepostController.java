package com.stu.edu.vn.backend.post.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.post.service.PostRepostService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Cung cấp tab Đăng lại trên hồ sơ bằng keyset cursor do Backend phát hành. */
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRepostController {
    private final PostRepostService postRepostService;

    @GetMapping("/{userId}/reposts")
    public ResponseEntity<ApiResponse<CursorPageResponse<FeedItemResponse>>> getReposts(
            @PathVariable @Positive Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách bài đăng lại thành công",
                postRepostService.getProfileReposts(userId, cursor, limit)));
    }
}
