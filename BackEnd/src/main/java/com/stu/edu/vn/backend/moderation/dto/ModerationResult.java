package com.stu.edu.vn.backend.moderation.dto;

import com.stu.edu.vn.backend.moderation.enums.ModerationCategory;
import com.stu.edu.vn.backend.moderation.enums.ModerationDecision;

/** Kết quả moderation nội bộ; không chứa raw response hoặc thông tin kỹ thuật của model. */
public record ModerationResult(
        ModerationDecision decision,
        ModerationCategory category,
        double confidence
) {
}
