package com.stu.edu.vn.backend.recommendation.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.recommendation.enums.StudentMatchReason;
import com.stu.edu.vn.backend.recommendation.repository.StudentRecommendationProjection;
import org.junit.jupiter.api.Test;

class StudentRecommendationMapperTest {

    private final StudentRecommendationMapper mapper = new StudentRecommendationMapper();

    @Test
    void mapsOnlyActualReasonsCountsAndAcademicIdentity() {
        StudentRecommendationProjection source = org.mockito.Mockito.mock(StudentRecommendationProjection.class);
        when(source.getUserId()).thenReturn(25L);
        when(source.getUsername()).thenReturn("nguyenvana");
        when(source.getDisplayName()).thenReturn("Nguyễn Văn A");
        when(source.getSchoolId()).thenReturn(1L);
        when(source.getSchoolName()).thenReturn("STU");
        when(source.getMajorId()).thenReturn(3L);
        when(source.getMajorName()).thenReturn("CNTT");
        when(source.getEntryYear()).thenReturn(2022);
        when(source.getSameSchool()).thenReturn(1);
        when(source.getSameFaculty()).thenReturn(0);
        when(source.getSameMajor()).thenReturn(1);
        when(source.getSameEntryYear()).thenReturn(0);
        when(source.getCommonInterestCount()).thenReturn(3);
        when(source.getMutualConnectionCount()).thenReturn(2);
        when(source.getMatchScore()).thenReturn(81);

        var response = mapper.toResponse(source);

        assertThat(response.matchReasons()).containsExactly(
                StudentMatchReason.SAME_SCHOOL,
                StudentMatchReason.SAME_MAJOR,
                StudentMatchReason.COMMON_INTERESTS,
                StudentMatchReason.MUTUAL_CONNECTIONS);
        assertThat(response.commonInterestCount()).isEqualTo(3);
        assertThat(response.mutualConnectionCount()).isEqualTo(2);
        assertThat(response.academic().school().id()).isEqualTo(1L);
        assertThat(response.academic().major().id()).isEqualTo(3L);
        assertThat(response.followedByMe()).isFalse();
    }
}
