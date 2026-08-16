package com.stu.edu.vn.backend.moderation.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.moderation.dto.ModerationAssessment;
import com.stu.edu.vn.backend.moderation.dto.ModerationErrorDetails;
import com.stu.edu.vn.backend.moderation.dto.ModerationResult;
import com.stu.edu.vn.backend.moderation.enums.ModerationCategory;
import com.stu.edu.vn.backend.moderation.enums.ModerationDecision;
import com.stu.edu.vn.backend.moderation.exception.ModerationProviderException;
import com.stu.edu.vn.backend.moderation.policy.ModerationPolicy;
import com.stu.edu.vn.backend.moderation.provider.ModerationProvider;
import com.stu.edu.vn.backend.moderation.service.ContentModerationService;
import org.springframework.stereotype.Service;

/** Điều phối provider và policy, đồng thời áp dụng fail-closed cho mọi lỗi inference. */
@Service
public class ContentModerationServiceImpl implements ContentModerationService {
    private final ModerationProvider moderationProvider;
    private final ModerationPolicy moderationPolicy;

    public ContentModerationServiceImpl(ModerationProvider moderationProvider, ModerationPolicy moderationPolicy) {
        this.moderationProvider = moderationProvider;
        this.moderationPolicy = moderationPolicy;
    }

    @Override
    public ModerationResult moderate(String content) {
        if (content == null || content.isBlank()) {
            return new ModerationResult(ModerationDecision.ALLOW, ModerationCategory.SAFE, 0d);
        }
        try {
            ModerationAssessment assessment = moderationProvider.moderate(content);
            return moderationPolicy.decide(assessment);
        } catch (ModerationProviderException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.CONTENT_MODERATION_UNAVAILABLE);
        }
    }

    @Override
    public void requireAllowed(String content) {
        ModerationResult result = moderate(content);
        if (result.decision() == ModerationDecision.WARNING) {
            throw new BusinessException(ErrorCode.CONTENT_MODERATION_WARNING, ModerationErrorDetails.from(result));
        }
        if (result.decision() == ModerationDecision.BLOCK) {
            throw new BusinessException(ErrorCode.CONTENT_POLICY_VIOLATION, ModerationErrorDetails.from(result));
        }
    }
}
