package com.stu.edu.vn.backend.feed.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.dto.FeedItemResponse;
import com.stu.edu.vn.backend.feed.service.FeedService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @GetMapping("/for-you")
    public ResponseEntity<ApiResponse<CursorPageResponse<FeedPostResponse>>> getForYou(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy Feed For You thành công",
                feedService.getForYou(cursor, limit)));
    }

    @GetMapping("/following")
    public ResponseEntity<ApiResponse<CursorPageResponse<FeedItemResponse>>> getFollowing(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy Feed Following thành công",
                feedService.getFollowing(cursor, limit)));
    }
}
