package com.stu.edu.vn.backend.moderation.provider;

import com.stu.edu.vn.backend.moderation.dto.ModerationAssessment;

/** Cổng inference dùng chung cho Post, Comment và Reply. */
public interface ModerationProvider {
    ModerationAssessment moderate(String text);
}
