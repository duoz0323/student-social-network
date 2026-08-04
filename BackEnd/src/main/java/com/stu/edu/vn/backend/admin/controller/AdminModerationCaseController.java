package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseActionRequest;
import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseNoViolationRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseStatusResponse;
import com.stu.edu.vn.backend.admin.service.AdminModerationCaseService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API ADMIN đọc và giải quyết trực tiếp Moderation Case, không có bước tiếp nhận trung gian. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/moderation-cases")
public class AdminModerationCaseController {
    private final AdminModerationCaseService service;

    public AdminModerationCaseController(AdminModerationCaseService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminModerationCaseListItemResponse>>> getCases(
            @RequestParam(required = false) ModerationCaseStatus status,
            @RequestParam(required = false) ReportReason reason,
            @RequestParam(required = false) @Positive Long postId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách hồ sơ kiểm duyệt thành công",
                service.getCases(status, reason, postId, keyword, fromDate, toDate, page, size)));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<AdminModerationCaseDetailResponse>> getCaseDetail(
            @PathVariable @Positive Long caseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết hồ sơ kiểm duyệt thành công", service.getCaseDetail(caseId)));
    }

    @PatchMapping("/{caseId}/resolve-no-violation")
    public ResponseEntity<ApiResponse<AdminModerationCaseStatusResponse>> resolveNoViolation(
            @PathVariable @Positive Long caseId,
            @Valid @RequestBody ResolveModerationCaseNoViolationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã kết luận bài viết không vi phạm", service.resolveNoViolation(caseId, request)));
    }

    @PatchMapping("/{caseId}/resolve-action")
    public ResponseEntity<ApiResponse<AdminModerationCaseStatusResponse>> resolveAction(
            @PathVariable @Positive Long caseId,
            @Valid @RequestBody ResolveModerationCaseActionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã xử lý vi phạm và giải quyết hồ sơ kiểm duyệt", service.resolveAction(caseId, request)));
    }
}
