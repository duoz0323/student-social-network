package com.stu.edu.vn.backend.admin.mapper;

import com.stu.edu.vn.backend.admin.dto.response.AdminActionAdminResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionListResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminActionTargetResponse;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionDetailProjection;
import com.stu.edu.vn.backend.admin.repository.projection.AdminActionListProjection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Ánh xạ lịch sử quản trị và lọc đệ quy các khóa JSON nhạy cảm. */
@Component
public class AdminActionMapper {
    private static final Set<String> SENSITIVE_JSON_KEYS = Set.of(
            "password", "passwordhash", "accesstoken", "refreshtoken", "tokenhash",
            "token", "secret", "clientsecret", "apikey", "authorization", "credentials"
    );

    private final ObjectMapper objectMapper;

    public AdminActionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AdminActionListResponse toListItem(
            AdminActionListProjection source,
            AdminActionTargetResponse target
    ) {
        AdminActionType actionType = AdminActionType.valueOf(source.getActionType());
        return new AdminActionListResponse(
                source.getActionId(), actionType, actionLabel(actionType), toAdmin(source), target,
                source.getNote(), source.getCreatedAt()
        );
    }

    public AdminActionDetailResponse toDetail(
            AdminActionDetailProjection source,
            AdminActionTargetResponse target
    ) {
        AdminActionType actionType = AdminActionType.valueOf(source.getActionType());
        return new AdminActionDetailResponse(
                source.getActionId(), actionType, actionLabel(actionType), toAdmin(source), target,
                source.getNote(), source.getCreatedAt(), parseAndSanitize(source.getOldData()),
                parseAndSanitize(source.getNewData())
        );
    }

    private AdminActionAdminResponse toAdmin(AdminActionListProjection source) {
        return new AdminActionAdminResponse(
                source.getAdminId(), source.getAdminDisplayName(), source.getAdminAvatarUrl()
        );
    }

    private String actionLabel(AdminActionType actionType) {
        return switch (actionType) {
            case BLOCK_USER -> "Khóa tài khoản";
            case UNBLOCK_USER -> "Mở khóa tài khoản";
            case HIDE_POST -> "Ẩn bài viết";
            case RESTORE_POST -> "Khôi phục bài viết";
            case RESOLVE_REPORT -> "Xác nhận báo cáo";
            case REJECT_REPORT -> "Từ chối báo cáo";
        };
    }

    private Object parseAndSanitize(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            return null;
        }
        try {
            Object parsed = objectMapper.readValue(rawData, Object.class);
            return sanitize(parsed);
        } catch (JacksonException exception) {
            // JSON sai cấu trúc là lỗi toàn vẹn dữ liệu; không trả chuỗi thô có thể chứa secret cho Client.
            throw new IllegalStateException("Dữ liệu lịch sử quản trị không phải JSON hợp lệ", exception);
        }
    }

    private Object sanitize(Object value) {
        if (value instanceof Map<?, ?> sourceMap) {
            Map<String, Object> safeMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                if (!(entry.getKey() instanceof String key) || isSensitiveKey(key)) {
                    continue;
                }
                safeMap.put(key, sanitize(entry.getValue()));
            }
            return safeMap;
        }
        if (value instanceof List<?> sourceList) {
            List<Object> safeList = new ArrayList<>(sourceList.size());
            sourceList.forEach(item -> safeList.add(sanitize(item)));
            return safeList;
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        String normalizedKey = key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        return SENSITIVE_JSON_KEYS.contains(normalizedKey);
    }
}
