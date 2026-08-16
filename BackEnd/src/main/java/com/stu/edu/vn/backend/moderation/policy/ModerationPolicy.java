package com.stu.edu.vn.backend.moderation.policy;

import com.stu.edu.vn.backend.moderation.dto.ModerationAssessment;
import com.stu.edu.vn.backend.moderation.dto.ModerationResult;
import com.stu.edu.vn.backend.moderation.enums.ModerationCategory;
import com.stu.edu.vn.backend.moderation.enums.ModerationClassification;
import com.stu.edu.vn.backend.moderation.enums.ModerationDecision;
import org.springframework.stereotype.Component;

/** Backend policy chuyển tín hiệu dự đoán thành đúng ba quyết định nghiệp vụ. */
@Component
public class ModerationPolicy {
    public ModerationResult decide(ModerationAssessment assessment) {
        if (assessment == null || assessment.category() == null || assessment.classification() == null
                || !Double.isFinite(assessment.confidence())
                || assessment.confidence() < 0d || assessment.confidence() > 1d) {
            throw new IllegalArgumentException("Moderation assessment không hợp lệ");
        }
        return switch (assessment.classification()) {
            case CLEAN -> new ModerationResult(
                    ModerationDecision.ALLOW, ModerationCategory.SAFE, assessment.confidence());
            case OFFENSIVE -> new ModerationResult(
                    ModerationDecision.WARNING, ModerationCategory.OFFENSIVE, assessment.confidence());
            case HATE -> new ModerationResult(
                    ModerationDecision.BLOCK, ModerationCategory.HATE, assessment.confidence());
        };
    }
}
