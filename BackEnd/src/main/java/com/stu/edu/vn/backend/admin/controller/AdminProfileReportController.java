package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.request.AdminProfileReportResolutionRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminProfileReportStatusResponse;
import com.stu.edu.vn.backend.admin.service.AdminProfileReportService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API ADMIN đọc và kết luận báo cáo trang cá nhân. */
@RestController
@RequestMapping("/api/v1/admin/profile-reports")
public class AdminProfileReportController {

    private final AdminProfileReportService service;

    public AdminProfileReportController(AdminProfileReportService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<AdminProfileReportListItemResponse>>> list(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách báo cáo trang cá nhân thành công",
                service.getReports(status, keyword, page, size)));
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("hasAuthority('REPORT_DETAIL_VIEW')")
    public ResponseEntity<ApiResponse<AdminProfileReportDetailResponse>> detail(@PathVariable Long caseId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết vụ việc báo cáo trang cá nhân thành công", service.getDetail(caseId)));
    }

    @PatchMapping("/{caseId}/reject")
    @PreAuthorize("hasAuthority('REPORT_RESOLVE_NO_VIOLATION')")
    public ResponseEntity<ApiResponse<AdminProfileReportStatusResponse>> reject(
            @PathVariable Long caseId,
            @Valid @RequestBody AdminProfileReportResolutionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã kết luận trang cá nhân không vi phạm", service.reject(caseId, request)));
    }

    @PatchMapping("/{caseId}/resolve")
    @PreAuthorize("hasAuthority('REPORT_RESOLVE_ACTION')")
    public ResponseEntity<ApiResponse<AdminProfileReportStatusResponse>> resolve(
            @PathVariable Long caseId,
            @Valid @RequestBody AdminProfileReportResolutionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã xác nhận trang cá nhân vi phạm", service.resolve(caseId, request)));
    }
}
