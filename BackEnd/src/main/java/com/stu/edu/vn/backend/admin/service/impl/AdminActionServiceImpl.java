package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.dto.response.AdminActionDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionListResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionTargetResponse;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.mapper.AdminActionMapper;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionListProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionPostTargetProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionReportTargetProjection;
import com.stu.edu.vn.backend.admin.service.AdminActionService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Truy vấn page tại database rồi resolve target theo batch, không query theo từng action. */
@Service
public class AdminActionServiceImpl implements AdminActionService {
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminActionRepository adminActionRepository;
    private final AdminActionMapper adminActionMapper;

    public AdminActionServiceImpl(
            AdminActionRepository adminActionRepository,
            AdminActionMapper adminActionMapper
    ) {
        this.adminActionRepository = adminActionRepository;
        this.adminActionMapper = adminActionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminActionListResponse> getActions(
            AdminActionType actionType,
            AdminTargetType targetType,
            Long adminId,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        validateFilters(adminId, from, to, page, size);
        Page<AdminActionListProjection> actions = adminActionRepository.findAdminActions(
                actionType == null ? null : actionType.name(),
                targetType == null ? null : targetType.name(),
                adminId, from, to, PageRequest.of(page, size)
        );
        Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> targets = resolveTargets(actions.getContent());
        return PageResponse.from(actions.map(action -> adminActionMapper.toListItem(
                action, targetFor(action, targets)
        )));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminActionDetailResponse getActionDetail(Long actionId) {
        if (actionId == null || actionId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        AdminActionDetailProjection action = adminActionRepository.findAdminActionDetail(actionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACTION_NOT_FOUND));
        Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> targets = resolveTargets(List.of(action));
        return adminActionMapper.toDetail(action, targetFor(action, targets));
    }
// trả về mục tiêu tương tác
    private Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> resolveTargets(
            List<? extends AdminActionListProjection> actions
    ) {
        Map<AdminTargetType, Set<Long>> idsByType = new EnumMap<>(AdminTargetType.class);
        for (AdminActionListProjection action : actions) {
            AdminTargetType type = AdminTargetType.valueOf(action.getTargetType());
            idsByType.computeIfAbsent(type, ignored -> new LinkedHashSet<>()).add(action.getTargetId());
        }

        Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result = new EnumMap<>(AdminTargetType.class);
        loadUserTargets(idsByType.get(AdminTargetType.USER), result);
        loadPostTargets(idsByType.get(AdminTargetType.POST), result);
        loadHashtagTargets(idsByType.get(AdminTargetType.HASHTAG), result);
        loadReportTargets(idsByType.get(AdminTargetType.REPORT), result);
        loadModerationCaseTargets(idsByType.get(AdminTargetType.MODERATION_CASE), result);
        loadProfileReportTargets(idsByType.get(AdminTargetType.PROFILE_REPORT), result);
        loadAcademicTargets(idsByType.get(AdminTargetType.ACADEMIC_DATA), result);
        return result;
    }

    private void loadAcademicTargets(
            Set<Long> targetIds,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result
    ) {
        if (targetIds == null || targetIds.isEmpty()) return;
        // Target cụ thể được lưu trong note vì bốn bảng master có thể trùng ID nội bộ.
        result.put(AdminTargetType.ACADEMIC_DATA, targetIds.stream().collect(Collectors.toMap(
                Function.identity(),
                id -> new AdminActionTargetResponse(
                        AdminTargetType.ACADEMIC_DATA, id, "Dữ liệu học thuật #" + id, true)
        )));
    }

    private void loadUserTargets(
            Set<Long> targetIds,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result
    ) {
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }
        Map<Long, AdminActionTargetResponse> targets = adminActionRepository.findUserTargets(targetIds).stream()
                .map(target -> new AdminActionTargetResponse(
                        AdminTargetType.USER,
                        target.getTargetId(),
                        hasText(target.getDisplayName()) ? target.getDisplayName() : userFallback(target.getTargetId()),
                        true
                ))
                .collect(Collectors.toMap(AdminActionTargetResponse::targetId, Function.identity()));
        result.put(AdminTargetType.USER, targets);
    }

    private void loadPostTargets(
            Set<Long> targetIds,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result
    ) {
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }
        Map<Long, AdminActionTargetResponse> targets = adminActionRepository.findPostTargets(targetIds).stream()
                .map(AdminActionPostTargetProjection::getTargetId)
                .map(id -> new AdminActionTargetResponse(AdminTargetType.POST, id, postFallback(id), true))
                .collect(Collectors.toMap(AdminActionTargetResponse::targetId, Function.identity()));
        result.put(AdminTargetType.POST, targets);
    }

    private void loadReportTargets(
            Set<Long> targetIds,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result
    ) {
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }
        Map<Long, AdminActionTargetResponse> targets = adminActionRepository.findReportTargets(targetIds).stream()
                .map(AdminActionReportTargetProjection::getTargetId)
                .map(id -> new AdminActionTargetResponse(AdminTargetType.REPORT, id, reportFallback(id), true))
                .collect(Collectors.toMap(AdminActionTargetResponse::targetId, Function.identity()));
        result.put(AdminTargetType.REPORT, targets);
    }

    private void loadModerationCaseTargets(
            Set<Long> targetIds,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result
    ) {
        if (targetIds == null || targetIds.isEmpty()) return;
        Map<Long, AdminActionTargetResponse> targets = adminActionRepository.findModerationCaseTargets(targetIds)
                .stream()
                .map(AdminActionPostTargetProjection::getTargetId)
                .map(id -> new AdminActionTargetResponse(
                        AdminTargetType.MODERATION_CASE, id, moderationCaseFallback(id), true))
                .collect(Collectors.toMap(AdminActionTargetResponse::targetId, Function.identity()));
        result.put(AdminTargetType.MODERATION_CASE, targets);
    }

    private void loadHashtagTargets(
            Set<Long> targetIds,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result
    ) {
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }
        Map<Long, AdminActionTargetResponse> targets = adminActionRepository.findHashtagTargets(targetIds).stream()
                .map(target -> new AdminActionTargetResponse(
                        AdminTargetType.HASHTAG,
                        target.getTargetId(),
                        hasText(target.getDisplayName()) ? "#" + target.getDisplayName() : hashtagFallback(target.getTargetId()),
                        true
                ))
                .collect(Collectors.toMap(AdminActionTargetResponse::targetId, Function.identity()));
        result.put(AdminTargetType.HASHTAG, targets);
    }

    private void loadProfileReportTargets(
            Set<Long> targetIds,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> result
    ) {
        if (targetIds == null || targetIds.isEmpty()) return;
        Map<Long, AdminActionTargetResponse> targets = adminActionRepository.findProfileReportTargets(targetIds)
                .stream()
                .map(AdminActionReportTargetProjection::getTargetId)
                .map(id -> new AdminActionTargetResponse(
                        AdminTargetType.PROFILE_REPORT, id, profileReportFallback(id), true))
                .collect(Collectors.toMap(AdminActionTargetResponse::targetId, Function.identity()));
        result.put(AdminTargetType.PROFILE_REPORT, targets);
    }

    private AdminActionTargetResponse targetFor(
            AdminActionListProjection action,
            Map<AdminTargetType, Map<Long, AdminActionTargetResponse>> targets
    ) {
        AdminTargetType type = AdminTargetType.valueOf(action.getTargetType());
        AdminActionTargetResponse available = targets.getOrDefault(type, Map.of()).get(action.getTargetId());
        if (available != null) {
            return available;
        }
        String displayText = switch (type) {
            case USER -> userFallback(action.getTargetId());
            case POST -> postFallback(action.getTargetId());
            case HASHTAG -> hashtagFallback(action.getTargetId());
            case REPORT -> reportFallback(action.getTargetId());
            case MODERATION_CASE -> moderationCaseFallback(action.getTargetId());
            case PROFILE_REPORT -> profileReportFallback(action.getTargetId());
            case ACADEMIC_DATA -> "Dữ liệu học thuật #" + action.getTargetId();
        };
        return new AdminActionTargetResponse(type, action.getTargetId(), displayText, false);
    }

    private void validateFilters(Long adminId, LocalDateTime from, LocalDateTime to, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE || (adminId != null && adminId <= 0)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Thời điểm bắt đầu không được sau thời điểm kết thúc");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String userFallback(Long id) {
        return "Người dùng #" + id;
    }

    private String postFallback(Long id) {
        return "Bài viết #" + id;
    }

    private String hashtagFallback(Long id) {
        return "Hashtag #" + id;
    }

    private String reportFallback(Long id) {
        return "Báo cáo #" + id;
    }

    private String moderationCaseFallback(Long id) {
        return "Hồ sơ kiểm duyệt #" + id;
    }

    private String profileReportFallback(Long id) {
        return "Báo cáo trang cá nhân #" + id;
    }
}
