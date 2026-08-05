package com.stu.edu.vn.backend.admin.mapper;

import com.stu.edu.vn.backend.admin.dto.response.AdminReportDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportEvidenceResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportPostSummaryResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportResolutionResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportResolvedByResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusPostResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportStatusResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportUserResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminReportedPostResponse;
import com.stu.edu.vn.backend.admin.repository.projection.AdminReportDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminReportListProjection;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Ánh xạ scalar projection sang contract Admin Report và parse snapshot JSON an toàn. */
@Component
public class AdminReportMapper {
    // biến này tạo ra để giữ lại kiểu dữ liệu của Json sau khi chuyển từ Json sang list
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() { };

    private final ObjectMapper objectMapper;

    public AdminReportMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AdminReportListItemResponse toListItem(AdminReportListProjection source) {
        AdminReportUserResponse reporter = new AdminReportUserResponse(
                source.getReporterId(), source.getReporterDisplayName(), source.getReporterAvatarUrl(),
                UserStatus.valueOf(source.getReporterAccountStatus()));
        AdminReportPostSummaryResponse post = new AdminReportPostSummaryResponse(
                source.getPostId(), PostStatus.valueOf(source.getPostCurrentStatus()), source.getContentPreview(),
                source.getAuthorId(), source.getAuthorDisplayName(),
                UserStatus.valueOf(source.getAuthorAccountStatus()));
        long mediaCount = source.getSnapshotMediaCount() == null ? 0L : source.getSnapshotMediaCount();
        return new AdminReportListItemResponse(source.getReportId(), ReportStatus.valueOf(source.getStatus()),
                ReportReason.valueOf(source.getReason()), source.getDescription(), reporter, post,
                mediaCount, source.getCreatedAt());
    }

    public AdminReportDetailResponse toDetail(AdminReportDetailProjection source) {
        AdminReportUserResponse reporter = new AdminReportUserResponse(
                source.getReporterId(), source.getReporterDisplayName(), source.getReporterAvatarUrl(),
                UserStatus.valueOf(source.getReporterAccountStatus()));
        AdminReportUserResponse author = new AdminReportUserResponse(
                source.getAuthorId(), source.getAuthorDisplayName(), source.getAuthorAvatarUrl(),
                UserStatus.valueOf(source.getAuthorAccountStatus()));
        AdminReportedPostResponse post = new AdminReportedPostResponse(
                source.getPostId(), PostStatus.valueOf(source.getPostCurrentStatus()),
                source.getPostCurrentContent(), author, source.getHiddenAt(), source.getHiddenReason(),
                source.getDeletedAt());
        AdminReportEvidenceResponse evidence = new AdminReportEvidenceResponse(
                source.getContentSnapshot(), parseMediaSnapshot(source.getMediaSnapshot()));
        AdminReportResolvedByResponse resolvedBy = source.getResolvedByAdminId() == null ? null
                : new AdminReportResolvedByResponse(
                        source.getResolvedByAdminId(), source.getResolvedByDisplayName());
        AdminReportResolutionResponse resolution = new AdminReportResolutionResponse(
                resolvedBy, source.getResolvedAt(), source.getResolutionNote());
        return new AdminReportDetailResponse(source.getReportId(), ReportStatus.valueOf(source.getStatus()),
                ReportReason.valueOf(source.getReason()), source.getDescription(), reporter, post,
                evidence, resolution, source.getCreatedAt());
    }

    public AdminReportStatusResponse toStatus(Report report, Post post, Long adminId, String displayName) {
        AdminReportResolvedByResponse resolvedBy = new AdminReportResolvedByResponse(adminId, displayName);
        AdminReportStatusPostResponse postResponse = new AdminReportStatusPostResponse(
                post.getId(), post.getStatus(), post.getHiddenAt(), post.getHiddenReason());
        return new AdminReportStatusResponse(report.getId(), report.getStatus(), report.getResolvedAt(),
                report.getResolutionNote(), resolvedBy, postResponse);
    }

    private List<String> parseMediaSnapshot(String rawSnapshot) {
        if (rawSnapshot == null || rawSnapshot.isBlank()) {
            return List.of();
        }
        try {
            // Snapshot do Backend tạo là mảng URL; đọc trực tiếp cột report để giữ đúng bằng chứng lịch sử.
            return List.copyOf(objectMapper.readValue(rawSnapshot, STRING_LIST_TYPE));
        } catch (JacksonException | NullPointerException exception) {
            // JSON trong database sai cấu trúc là lỗi toàn vẹn dữ liệu, không được âm thầm thay bằng media hiện tại.
            throw new IllegalStateException("Snapshot media của báo cáo không hợp lệ", exception);
        }
    }
}
