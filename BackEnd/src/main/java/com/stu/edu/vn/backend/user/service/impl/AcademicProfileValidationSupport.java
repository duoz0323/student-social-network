package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.academic.entity.Faculty;
import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.entity.School;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.academic.repository.FacultyRepository;
import com.stu.edu.vn.backend.academic.repository.InterestCategoryRepository;
import com.stu.edu.vn.backend.academic.repository.MajorRepository;
import com.stu.edu.vn.backend.academic.repository.SchoolRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.dto.request.AcademicProfileRequest;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Validate hierarchy và master data ACTIVE trước khi thay đổi hồ sơ đã khóa. */
@Component
@RequiredArgsConstructor
class AcademicProfileValidationSupport {
    static final int MIN_ENTRY_YEAR = 1900;
    static final int MAX_INTERESTS = 10;

    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final MajorRepository majorRepository;
    private final InterestCategoryRepository interestCategoryRepository;
    private final Clock clock;

    void applyAcademic(UserProfile profile, AcademicProfileRequest request) {
        if (request == null) {
            // Client cũ không gửi khối academic thì phải giữ nguyên dữ liệu hiện có.
            return;
        }
        if (request.facultyId() != null && request.schoolId() == null) {
            throw new BusinessException(ErrorCode.ACADEMIC_FACULTY_SCHOOL_MISMATCH);
        }
        if (request.majorId() != null && request.facultyId() == null) {
            throw new BusinessException(ErrorCode.ACADEMIC_MAJOR_FACULTY_MISMATCH);
        }

        School school = request.schoolId() == null ? null
                : schoolRepository.findByIdAndStatus(request.schoolId(), AcademicStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMIC_SCHOOL_INVALID));
        Faculty faculty = request.facultyId() == null ? null
                : facultyRepository.findByIdAndStatus(request.facultyId(), AcademicStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMIC_FACULTY_INVALID));
        if (faculty != null && !faculty.getSchool().getId().equals(school.getId())) {
            throw new BusinessException(ErrorCode.ACADEMIC_FACULTY_SCHOOL_MISMATCH);
        }

        Major major = request.majorId() == null ? null
                : majorRepository.findByIdAndStatus(request.majorId(), AcademicStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMIC_MAJOR_INVALID));
        if (major != null && !major.getFaculty().getId().equals(faculty.getId())) {
            throw new BusinessException(ErrorCode.ACADEMIC_MAJOR_FACULTY_MISMATCH);
        }

        validateEntryYear(request.entryYear());
        profile.setSchool(school);
        profile.setFaculty(faculty);
        profile.setMajor(major);
        profile.setEntryYear(request.entryYear());
    }

    void applyInterests(UserProfile profile, List<Long> interestIds) {
        if (interestIds == null) {
            // null nghĩa là giữ nguyên; danh sách rỗng mới là yêu cầu xóa toàn bộ sở thích.
            return;
        }
        if (interestIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(ErrorCode.INTEREST_INVALID);
        }
        Set<Long> distinctIds = new LinkedHashSet<>(interestIds);
        if (distinctIds.size() > MAX_INTERESTS) {
            throw new BusinessException(ErrorCode.INTEREST_LIMIT_EXCEEDED);
        }
        List<InterestCategory> interests = distinctIds.isEmpty()
                ? List.of()
                : interestCategoryRepository.findAllByIdInAndStatus(distinctIds, AcademicStatus.ACTIVE);
        if (interests.size() != distinctIds.size()) {
            throw new BusinessException(ErrorCode.INTEREST_INVALID);
        }
        profile.getInterests().clear();
        profile.getInterests().addAll(interests);
    }

    private void validateEntryYear(Integer entryYear) {
        int currentYear = LocalDate.now(clock).getYear();
        if (entryYear != null && (entryYear < MIN_ENTRY_YEAR || entryYear > currentYear)) {
            throw new BusinessException(ErrorCode.ACADEMIC_ENTRY_YEAR_INVALID);
        }
    }
}
