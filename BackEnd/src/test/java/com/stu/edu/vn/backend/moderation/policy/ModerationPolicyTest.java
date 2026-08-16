package com.stu.edu.vn.backend.moderation.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.moderation.dto.ModerationAssessment;
import com.stu.edu.vn.backend.moderation.enums.ModerationClassification;
import com.stu.edu.vn.backend.moderation.enums.ModerationDecision;
import org.junit.jupiter.api.Test;

class ModerationPolicyTest {

    private final ModerationPolicy policy = new ModerationPolicy();

    @Test
    void mapsTheThreeLocalModelLabelsDirectlyToBusinessDecisions() {
        assertThat(policy.decide(ModerationAssessment.fromLocalModel(
                ModerationClassification.CLEAN, 0.99d)).decision()).isEqualTo(ModerationDecision.ALLOW);
        assertThat(policy.decide(ModerationAssessment.fromLocalModel(
                ModerationClassification.OFFENSIVE, 0.51d)).decision()).isEqualTo(ModerationDecision.WARNING);
        assertThat(policy.decide(ModerationAssessment.fromLocalModel(
                ModerationClassification.HATE, 0.51d)).decision()).isEqualTo(ModerationDecision.BLOCK);
    }

}
