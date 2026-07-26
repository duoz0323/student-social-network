package com.stu.edu.vn.backend.post.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.post.service.UserPostService;
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

/** API danh sách bài công khai trên hồ sơ, tách khỏi API cập nhật hồ sơ cá nhân. */
@Validated
@RestController
@RequestMapping("/api/v1/users/{userId}/posts")
@RequiredArgsConstructor
public class UserPostController {
    private final UserPostService userPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<FeedPostResponse>>> getUserPosts(
            @PathVariable @Positive Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách bài viết trên hồ sơ thành công",
                userPostService.getUserPosts(userId, cursor, limit)
        ));
    }
}
