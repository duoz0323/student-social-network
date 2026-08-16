package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.response.AdminActionDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionListResponse;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.service.AdminActionService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API chỉ đọc lịch sử thao tác dành cho ADMIN. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/actions")
public class AdminActionController {
    private final AdminActionService adminActionService;

    public AdminActionController(AdminActionService adminActionService) {
        this.adminActionService = adminActionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN_ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminActionListResponse>>> getActions(
            @RequestParam(required = false) AdminActionType actionType,
            @RequestParam(required = false) AdminTargetType targetType,
            @RequestParam(required = false) @Positive Long adminId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validateInputs(adminId, from, to, page, size);
        PageResponse<AdminActionListResponse> response = adminActionService.getActions(
                actionType, targetType, adminId, from, to, page, size
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử thao tác quản trị thành công", response));
    }

    @GetMapping("/{actionId}")
    @PreAuthorize("hasAuthority('ADMIN_ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminActionDetailResponse>> getActionDetail(
            @PathVariable @Positive Long actionId
    ) {
        if (actionId == null || actionId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        AdminActionDetailResponse response = adminActionService.getActionDetail(actionId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thao tác quản trị thành công", response));
    }

    private void validateInputs(Long adminId, LocalDateTime from, LocalDateTime to, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (adminId != null && adminId <= 0)
                || (from != null && to != null && from.isAfter(to))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
