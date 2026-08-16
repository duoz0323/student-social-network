package com.stu.edu.vn.backend.moderation.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.moderation.dto.ModerationAssessment;
import com.stu.edu.vn.backend.moderation.enums.ModerationClassification;
import com.stu.edu.vn.backend.moderation.enums.ModerationDecision;
import com.stu.edu.vn.backend.moderation.exception.ModerationProviderException;
import com.stu.edu.vn.backend.moderation.policy.ModerationPolicy;
import com.stu.edu.vn.backend.moderation.provider.ModerationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContentModerationServiceImplTest {
    private final ModerationProvider provider = org.mockito.Mockito.mock(ModerationProvider.class);
    private ContentModerationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentModerationServiceImpl(provider, new ModerationPolicy());
    }

    @Test
    void safeContentIsAllowed() {
        when(provider.moderate("Nội dung an toàn"))
                .thenReturn(ModerationAssessment.fromLocalModel(ModerationClassification.CLEAN, 0.9d));
        assertThat(service.moderate("Nội dung an toàn").decision()).isEqualTo(ModerationDecision.ALLOW);
    }

    @Test
    void warningAndBlockExposeOnlyStableDecisionAndCategory() {
        when(provider.moderate("borderline"))
                .thenReturn(ModerationAssessment.fromLocalModel(ModerationClassification.OFFENSIVE, 0.6d));
        when(provider.moderate("violation"))
                .thenReturn(ModerationAssessment.fromLocalModel(ModerationClassification.HATE, 0.9d));

        assertThatThrownBy(() -> service.requireAllowed("borderline"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONTENT_MODERATION_WARNING);
        assertThatThrownBy(() -> service.requireAllowed("violation"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONTENT_POLICY_VIOLATION);
    }

    @Test
    void providerTimeoutUnavailableAndInvalidResponseFailClosed() {
        when(provider.moderate("timeout")).thenThrow(new ModerationProviderException("timeout"));
        when(provider.moderate("invalid")).thenReturn(null);

        assertUnavailable("timeout");
        assertUnavailable("invalid");
    }

    private void assertUnavailable(String content) {
        assertThatThrownBy(() -> service.requireAllowed(content))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONTENT_MODERATION_UNAVAILABLE);
    }
}
