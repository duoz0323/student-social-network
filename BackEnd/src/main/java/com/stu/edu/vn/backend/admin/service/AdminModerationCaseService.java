package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseActionRequest;
import com.stu.edu.vn.backend.admin.dto.request.ResolveModerationCaseNoViolationRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseStatusResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import java.time.LocalDate;

/** Use case đọc và xử lý Moderation Case dành riêng cho ADMIN. */
public interface AdminModerationCaseService {
    PageResponse<AdminModerationCaseListItemResponse> getCases(
            ModerationCaseStatus status,
            ReportReason reason,
            Long postId,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size);

    AdminModerationCaseDetailResponse getCaseDetail(Long caseId);
// không vi phạm
    AdminModerationCaseStatusResponse resolveNoViolation(
            Long caseId,
            ResolveModerationCaseNoViolationRequest request);

    AdminModerationCaseStatusResponse resolveAction(
            Long caseId,
            ResolveModerationCaseActionRequest request);
}
