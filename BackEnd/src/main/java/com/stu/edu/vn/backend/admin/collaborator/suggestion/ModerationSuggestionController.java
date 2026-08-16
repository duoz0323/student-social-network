package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import com.stu.edu.vn.backend.common.api.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
public class ModerationSuggestionController {
    private final ModerationSuggestionService service;

    @PostMapping("/api/v1/admin/collaborator/moderation-suggestions")
    @PreAuthorize("hasAuthority('COLLABORATOR_MODERATION_SUGGEST')")
    public ResponseEntity<ApiResponse<ModerationSuggestionResponse>> create(
            @Valid @RequestBody CreateModerationSuggestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi đề xuất kiểm duyệt thành công", service.create(request)));
    }

    @GetMapping("/api/v1/admin/collaborator/moderation-suggestions")
    @PreAuthorize("hasAuthority('COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN')")
    public ResponseEntity<ApiResponse<PageResponse<ModerationSuggestionResponse>>> own(
            @RequestParam(required = false) ModerationSuggestionStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy đề xuất kiểm duyệt thành công",
                service.getOwn(status, page, size)));
    }

    @GetMapping("/api/v1/admin/moderation-suggestions")
    @PreAuthorize("hasAuthority('MODERATION_SUGGESTION_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<ModerationSuggestionResponse>>> all(
            @RequestParam(required = false) ModerationSuggestionStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đề xuất kiểm duyệt thành công",
                service.getAll(status, page, size)));
    }

    @GetMapping("/api/v1/admin/moderation-suggestions/{id}")
    @PreAuthorize("hasAuthority('MODERATION_SUGGESTION_DETAIL_VIEW')")
    public ResponseEntity<ApiResponse<ModerationSuggestionResponse>> detail(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đề xuất thành công", service.get(id)));
    }

    @PatchMapping("/api/v1/admin/moderation-suggestions/{id}/accept")
    @PreAuthorize("hasAuthority('MODERATION_SUGGESTION_REVIEW')")
    public ResponseEntity<ApiResponse<ModerationSuggestionResponse>> accept(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận đề xuất thành công",
                service.review(id, ModerationSuggestionStatus.ACCEPTED)));
    }

    @PatchMapping("/api/v1/admin/moderation-suggestions/{id}/reject")
    @PreAuthorize("hasAuthority('MODERATION_SUGGESTION_REVIEW')")
    public ResponseEntity<ApiResponse<ModerationSuggestionResponse>> reject(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success("Từ chối đề xuất thành công",
                service.review(id, ModerationSuggestionStatus.REJECTED)));
    }
}
