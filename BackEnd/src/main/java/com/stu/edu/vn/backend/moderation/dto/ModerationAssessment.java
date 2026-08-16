package com.stu.edu.vn.backend.moderation.dto;

import com.stu.edu.vn.backend.moderation.enums.ModerationCategory;
import com.stu.edu.vn.backend.moderation.enums.ModerationClassification;

/** Tín hiệu provider đã được adapter chuẩn hóa trước khi business policy đưa ra quyết định. */
public record ModerationAssessment(
        ModerationCategory category,
        double confidence,
        ModerationClassification classification
) {
    public static ModerationAssessment fromLocalModel(
            ModerationClassification classification,
            double confidence
    ) {
        return switch (classification) {
            case CLEAN -> new ModerationAssessment(ModerationCategory.SAFE, confidence, classification);
            case OFFENSIVE -> new ModerationAssessment(ModerationCategory.OFFENSIVE, confidence, classification);
            case HATE -> new ModerationAssessment(ModerationCategory.HATE, confidence, classification);
        };
    }
}
