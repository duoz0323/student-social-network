package com.stu.edu.vn.backend.moderation.dto;

import com.stu.edu.vn.backend.moderation.enums.ModerationCategory;
import com.stu.edu.vn.backend.moderation.enums.ModerationDecision;

/** Chi tiết an toàn cho Frontend; cố ý không expose confidence hoặc dữ liệu provider. */
public record ModerationErrorDetails(
        ModerationDecision decision,
        ModerationCategory category
) {
    public static ModerationErrorDetails from(ModerationResult result) {
        return new ModerationErrorDetails(result.decision(), result.category());
    }
}
