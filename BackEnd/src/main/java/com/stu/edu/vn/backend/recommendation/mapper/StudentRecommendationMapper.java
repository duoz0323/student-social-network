package com.stu.edu.vn.backend.recommendation.mapper;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import com.stu.edu.vn.backend.recommendation.dto.response.StudentRecommendationAcademicResponse;
import com.stu.edu.vn.backend.recommendation.dto.response.StudentRecommendationResponse;
import com.stu.edu.vn.backend.recommendation.enums.StudentMatchReason;
import com.stu.edu.vn.backend.recommendation.repository.StudentRecommendationProjection;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Chuyển projection đã xếp hạng thành contract công khai và lý do semantic. */
@Component
public class StudentRecommendationMapper {

    public StudentRecommendationResponse toResponse(StudentRecommendationProjection source) {
        return new StudentRecommendationResponse(
                source.getUserId(),
                source.getUsername(),
                source.getDisplayName(),
                source.getAvatarUrl(),
                new StudentRecommendationAcademicResponse(
                        school(source),
                        academicItem(source.getFacultyId(), source.getFacultyName()),
                        academicItem(source.getMajorId(), source.getMajorName()),
                        source.getEntryYear()
                ),
                value(source.getMatchScore()),
                matchReasons(source),
                value(source.getCommonInterestCount()),
                value(source.getMutualConnectionCount()),
                false
        );
    }

    private List<StudentMatchReason> matchReasons(StudentRecommendationProjection source) {
        List<StudentMatchReason> reasons = new ArrayList<>();
        // Giữ thứ tự semantic ổn định; Frontend có thể ưu tiên trình bày mà không suy đoán từ score.
        addIfMatched(reasons, source.getSameSchool(), StudentMatchReason.SAME_SCHOOL);
        addIfMatched(reasons, source.getSameFaculty(), StudentMatchReason.SAME_FACULTY);
        addIfMatched(reasons, source.getSameMajor(), StudentMatchReason.SAME_MAJOR);
        addIfMatched(reasons, source.getSameEntryYear(), StudentMatchReason.SAME_ENTRY_YEAR);
        addIfMatched(reasons, source.getCommonInterestCount(), StudentMatchReason.COMMON_INTERESTS);
        addIfMatched(reasons, source.getMutualConnectionCount(), StudentMatchReason.MUTUAL_CONNECTIONS);
        return List.copyOf(reasons);
    }

    private void addIfMatched(List<StudentMatchReason> reasons, Integer value, StudentMatchReason reason) {
        if (value(value) > 0) {
            reasons.add(reason);
        }
    }

    private SchoolResponse school(StudentRecommendationProjection source) {
        return source.getSchoolId() == null
                ? null
                : new SchoolResponse(source.getSchoolId(), source.getSchoolName(), source.getSchoolShortName());
    }

    private AcademicItemResponse academicItem(Long id, String name) {
        return id == null ? null : new AcademicItemResponse(id, name);
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}
