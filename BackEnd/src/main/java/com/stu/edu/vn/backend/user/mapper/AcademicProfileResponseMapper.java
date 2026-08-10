package com.stu.edu.vn.backend.user.mapper;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.InterestResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import com.stu.edu.vn.backend.academic.entity.Faculty;
import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.entity.School;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Chuyển master entity sang DTO ổn định mà không trả trực tiếp JPA Entity. */
public final class AcademicProfileResponseMapper {
    private AcademicProfileResponseMapper() {
    }

    public static SchoolResponse school(School value) {
        return value == null ? null : new SchoolResponse(value.getId(), value.getName(), value.getShortName());
    }

    public static AcademicItemResponse faculty(Faculty value) {
        return value == null ? null : new AcademicItemResponse(value.getId(), value.getName());
    }

    public static AcademicItemResponse major(Major value) {
        return value == null ? null : new AcademicItemResponse(value.getId(), value.getName());
    }

    public static List<InterestResponse> interests(Collection<InterestCategory> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .sorted(Comparator.comparing(InterestCategory::getName).thenComparing(InterestCategory::getId))
                .map(value -> new InterestResponse(value.getId(), value.getName()))
                .toList();
    }
}
