package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import java.util.List;

/** Chi tiết conflict an toàn; không trả target user hoặc provider subject. */
public record SocialConflictDetails(
        String flowToken,
        String flowType,
        SocialConflictType conflictType,
        List<SocialResolutionAction> allowedActions,
        long expiresIn
) {
    public static final String FLOW_TYPE = "SOCIAL_CONFLICT";
}
