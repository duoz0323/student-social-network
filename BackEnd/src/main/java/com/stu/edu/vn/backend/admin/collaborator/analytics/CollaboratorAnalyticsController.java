package com.stu.edu.vn.backend.admin.collaborator.analytics;

import com.stu.edu.vn.backend.common.api.*;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/admin/collaborator")
@RequiredArgsConstructor
public class CollaboratorAnalyticsController {
    private final CollaboratorAnalyticsService service;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('COLLABORATOR_DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<CollaboratorDashboardResponse>> dashboard(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success("Lấy Dashboard thành công", service.dashboard(days)));
    }

    @GetMapping("/posts")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_VIEW_OWN')")
    public ResponseEntity<ApiResponse<PageResponse<CollaboratorPostListItem>>> posts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "NEWEST") String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy nội dung của tôi thành công",
                service.posts(keyword, status, sort, page, size)));
    }

    @GetMapping("/posts/{postId}/analytics")
    @PreAuthorize("hasAuthority('COLLABORATOR_POST_ANALYTICS_VIEW')")
    public ResponseEntity<ApiResponse<CollaboratorPostAnalyticsResponse>> analytics(
            @PathVariable @Positive Long postId,
            @RequestParam(defaultValue = "7D") String range) {
        return ResponseEntity.ok(ApiResponse.success("Lấy analytics bài viết thành công",
                service.analytics(postId, range)));
    }
}
