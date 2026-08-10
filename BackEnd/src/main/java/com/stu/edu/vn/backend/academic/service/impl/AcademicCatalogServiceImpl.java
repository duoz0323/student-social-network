package com.stu.edu.vn.backend.academic.service.impl;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.InterestResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.academic.repository.FacultyRepository;
import com.stu.edu.vn.backend.academic.repository.InterestCategoryRepository;
import com.stu.edu.vn.backend.academic.repository.MajorRepository;
import com.stu.edu.vn.backend.academic.repository.SchoolRepository;
import com.stu.edu.vn.backend.academic.service.AcademicCatalogService;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Query master data tại MySQL, không tải toàn bảng rồi lọc bằng Java. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicCatalogServiceImpl implements AcademicCatalogService {
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final int MAX_KEYWORD_LENGTH = 100;

    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final MajorRepository majorRepository;
    private final InterestCategoryRepository interestCategoryRepository;

    @Override
    public List<SchoolResponse> searchSchools(String keyword, Integer limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return schoolRepository.searchActive(
                        AcademicStatus.ACTIVE,
                        LikePatternEscaper.escape(normalizedKeyword),
                        PageRequest.of(0, normalizeLimit(limit))
                ).stream()
                .map(school -> new SchoolResponse(school.getId(), school.getName(), school.getShortName()))
                .toList();
    }

    @Override
    public List<AcademicItemResponse> searchFaculties(Long schoolId, String keyword, Integer limit) {
        schoolRepository.findByIdAndStatus(schoolId, AcademicStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMIC_SCHOOL_INVALID));
        return facultyRepository.searchActiveBySchool(
                        schoolId,
                        AcademicStatus.ACTIVE,
                        LikePatternEscaper.escape(normalizeKeyword(keyword)),
                        PageRequest.of(0, normalizeLimit(limit))
                ).stream()
                .map(faculty -> new AcademicItemResponse(faculty.getId(), faculty.getName()))
                .toList();
    }

    @Override
    public List<AcademicItemResponse> searchMajors(Long facultyId, String keyword, Integer limit) {
        facultyRepository.findByIdAndStatus(facultyId, AcademicStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMIC_FACULTY_INVALID));
        return majorRepository.searchActiveByFaculty(
                        facultyId,
                        AcademicStatus.ACTIVE,
                        LikePatternEscaper.escape(normalizeKeyword(keyword)),
                        PageRequest.of(0, normalizeLimit(limit))
                ).stream()
                .map(major -> new AcademicItemResponse(major.getId(), major.getName()))
                .toList();
    }

    @Override
    public List<InterestResponse> getInterests() {
        return interestCategoryRepository.findAllByStatusOrderByNameAsc(AcademicStatus.ACTIVE).stream()
                .map(interest -> new InterestResponse(interest.getId(), interest.getName()))
                .toList();
    }

    private int normalizeLimit(Integer limit) {
        int resolved = limit == null ? DEFAULT_LIMIT : limit;
        if (resolved < 1 || resolved > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.ACADEMIC_LIMIT_INVALID);
        }
        return resolved;
    }

    private String normalizeKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.ACADEMIC_KEYWORD_TOO_LONG);
        }
        return normalized;
    }
}
