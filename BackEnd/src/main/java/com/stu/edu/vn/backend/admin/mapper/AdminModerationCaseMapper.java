package com.stu.edu.vn.backend.admin.mapper;

import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseActionResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminModerationCaseReportResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportEvidenceResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportedPostResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportResolutionResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportResolvedByResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportUserResponse;
import com.stu.edu.vn.backend.admin.dto.response.ModerationReasonCountResponse;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseActionProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminModerationCaseReportProjection;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Mapper contract Moderation Case; parse snapshot từng Report và không trả dữ liệu xác thực. */
@Component
public class AdminModerationCaseMapper {
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() { };
    private final ObjectMapper objectMapper;

    public AdminModerationCaseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AdminModerationCaseListItemResponse toListItem(
            AdminModerationCaseListProjection source,
            Map<Long, List<ModerationReasonCountResponse>> reasonsByCase
    ) {
        return new AdminModerationCaseListItemResponse(
                source.getCaseId(), source.getPostId(), source.getPostContentPreview(),
                source.getPostAuthorId(), source.getPostAuthorDisplayName(), safeLong(source.getReportCount()),
                safeLong(source.getDistinctReporterCount()),
                reasonsByCase.getOrDefault(source.getCaseId(), List.of()),
                ModerationCaseStatus.valueOf(source.getStatus()), source.getFirstReportedAt(),
                source.getLatestReportedAt(), source.getResolvedBy(), source.getResolvedByDisplayName(),
                source.getResolvedAt());
    }

    public AdminModerationCaseDetailResponse toDetail(
            AdminModerationCaseDetailProjection source,
            List<ModerationReasonCountResponse> reasons,
            List<AdminModerationCaseReportProjection> reportSources,
            List<AdminModerationCaseActionProjection> actionSources
    ) {
        AdminReportUserResponse author = new AdminReportUserResponse(
                source.getAuthorId(), source.getAuthorDisplayName(), source.getAuthorAvatarUrl(),
                UserStatus.valueOf(source.getAuthorAccountStatus()));
        AdminReportedPostResponse post = new AdminReportedPostResponse(
                source.getPostId(), PostStatus.valueOf(source.getPostCurrentStatus()),
                source.getPostCurrentContent(), author, source.getHiddenAt(), source.getHiddenReason(),
                source.getDeletedAt());
        AdminReportResolvedByResponse resolvedBy = source.getResolvedByAdminId() == null ? null
                : new AdminReportResolvedByResponse(source.getResolvedByAdminId(), source.getResolvedByDisplayName());
        AdminReportResolutionResponse resolution = new AdminReportResolutionResponse(
                resolvedBy, source.getResolvedAt(), source.getResolutionNote());
        return new AdminModerationCaseDetailResponse(
                source.getCaseId(), ModerationCaseStatus.valueOf(source.getStatus()),
                safeLong(source.getReportCount()), safeLong(source.getDistinctReporterCount()),
                source.getFirstReportedAt(), source.getLatestReportedAt(), post, reasons,
                reportSources.stream().map(this::toReport).toList(), resolution,
                actionSources.stream().map(this::toAction).toList());
    }

    private AdminModerationCaseReportResponse toReport(AdminModerationCaseReportProjection source) {
        AdminReportUserResponse reporter = new AdminReportUserResponse(
                source.getReporterId(), source.getReporterDisplayName(), source.getReporterAvatarUrl(),
                UserStatus.valueOf(source.getReporterAccountStatus()));
        AdminReportEvidenceResponse evidence = new AdminReportEvidenceResponse(
                source.getContentSnapshot(), parseMediaSnapshot(source.getMediaSnapshot()));
        return new AdminModerationCaseReportResponse(
                source.getReportId(), reporter, ReportReason.valueOf(source.getReason()), source.getDescription(),
                ReportStatus.valueOf(source.getStatus()), evidence, source.getCreatedAt(), source.getResolvedAt());
    }

    private AdminModerationCaseActionResponse toAction(AdminModerationCaseActionProjection source) {
        return new AdminModerationCaseActionResponse(
                source.getActionId(), AdminActionType.valueOf(source.getActionType()), source.getAdminId(),
                source.getAdminDisplayName(), source.getNote(), source.getCreatedAt());
    }

    private List<String> parseMediaSnapshot(String rawSnapshot) {
        if (rawSnapshot == null || rawSnapshot.isBlank()) return List.of();
        try {
            return List.copyOf(objectMapper.readValue(rawSnapshot, STRING_LIST_TYPE));
        } catch (JacksonException | NullPointerException exception) {
            throw new IllegalStateException("Snapshot media của báo cáo không hợp lệ", exception);
        }
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
