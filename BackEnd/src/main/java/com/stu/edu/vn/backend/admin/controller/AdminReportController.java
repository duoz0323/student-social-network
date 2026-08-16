package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.request.AdminRejectReportRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminResolveReportRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusResponse;
import com.stu.edu.vn.backend.admin.service.AdminReportService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API để ADMIN đọc, xác nhận hoặc từ chối báo cáo theo state machine kiểm duyệt. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {
    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<AdminReportListItemResponse>>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReason reason,
            @RequestParam(required = false) @Positive Long postId,
            @RequestParam(required = false) @Positive Long reporterId,
            @RequestParam(required = false) @Positive Long authorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validateInputs(postId, reporterId, authorId, page, size);
        PageResponse<AdminReportListItemResponse> response = adminReportService.getReports(
                status, reason, postId, reporterId, authorId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách báo cáo quản trị thành công", response));
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAuthority('REPORT_DETAIL_VIEW')")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> getReportDetail(
            @PathVariable @Positive Long reportId
    ) {
        validateReportId(reportId);
        AdminReportDetailResponse response = adminReportService.getReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết báo cáo quản trị thành công", response));
    }

    @PatchMapping("/{reportId}/reject")
    @PreAuthorize("hasAuthority('REPORT_RESOLVE_NO_VIOLATION')")
    public ResponseEntity<ApiResponse<AdminReportStatusResponse>> rejectReport(
            @PathVariable @Positive Long reportId,
            @RequestBody AdminRejectReportRequest request
    ) {
        validateReportId(reportId);
        AdminReportStatusResponse response = adminReportService.rejectReport(reportId, request);
        return ResponseEntity.ok(ApiResponse.success("Từ chối báo cáo thành công", response));
    }

    @PatchMapping("/{reportId}/resolve")
    @PreAuthorize("hasAuthority('REPORT_RESOLVE_ACTION')")
    public ResponseEntity<ApiResponse<AdminReportStatusResponse>> resolveReport(
            @PathVariable @Positive Long reportId,
            @RequestBody AdminResolveReportRequest request
    ) {
        validateReportId(reportId);
        AdminReportStatusResponse response = adminReportService.resolveReport(reportId, request);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận báo cáo hợp lệ thành công", response));
    }

    private void validateReportId(Long reportId) {
        if (reportId == null || reportId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateInputs(Long postId, Long reporterId, Long authorId, int page, int size) {
        // Kiểm tra phòng thủ để hành vi validation ổn định cả khi method-validation proxy chưa hoạt động.
        if (page < 0 || size < 1 || size > 100
                || isInvalidId(postId) || isInvalidId(reporterId) || isInvalidId(authorId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private boolean isInvalidId(Long id) {
        return id != null && id <= 0;
    }
}
