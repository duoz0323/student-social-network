package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.academic.entity.Faculty;
import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.entity.School;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.academic.repository.FacultyRepository;
import com.stu.edu.vn.backend.academic.repository.InterestCategoryRepository;
import com.stu.edu.vn.backend.academic.repository.MajorRepository;
import com.stu.edu.vn.backend.academic.repository.SchoolRepository;
import com.stu.edu.vn.backend.admin.dto.response.AdminFacultyResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminInterestResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminMajorResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminSchoolResponse;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.service.AdminAcademicService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.text.Normalizer;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quản trị master data bằng transaction ngắn, không cascade status và không xóa quan hệ người dùng. */
@Service
public class AdminAcademicServiceImpl implements AdminAcademicService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_ACADEMIC_NAME_LENGTH = 255;
    private static final int MAX_INTEREST_NAME_LENGTH = 100;
    private static final int MAX_SHORT_NAME_LENGTH = 50;

    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final MajorRepository majorRepository;
    private final InterestCategoryRepository interestRepository;
    private final AdminActionRepository adminActionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EntityManager entityManager;

    public AdminAcademicServiceImpl(
            SchoolRepository schoolRepository,
            FacultyRepository facultyRepository,
            MajorRepository majorRepository,
            InterestCategoryRepository interestRepository,
            AdminActionRepository adminActionRepository,
            CurrentUserProvider currentUserProvider,
            EntityManager entityManager
    ) {
        this.schoolRepository = schoolRepository;
        this.facultyRepository = facultyRepository;
        this.majorRepository = majorRepository;
        this.interestRepository = interestRepository;
        this.adminActionRepository = adminActionRepository;
        this.currentUserProvider = currentUserProvider;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminSchoolResponse> getSchools(String keyword, int page, int size) {
        validatePagination(page, size);
        return PageResponse.from(schoolRepository.searchForAdmin(
                normalizeKeyword(keyword), PageRequest.of(page, size)).map(this::toSchoolResponse));
    }

    @Override
    @Transactional
    public AdminSchoolResponse createSchool(String name, String shortName) {
        CustomUserPrincipal principal = requireActiveAdmin();
        String normalizedName = normalizeRequiredName(name, MAX_ACADEMIC_NAME_LENGTH);
        String normalizedShortName = normalizeOptionalName(shortName, MAX_SHORT_NAME_LENGTH);
        if (schoolRepository.existsByName(normalizedName)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_SCHOOL_ALREADY_EXISTS);
        }
        School school = saveSchool(new School(normalizedName, normalizedShortName));
        saveAudit(principal, AdminActionType.CREATE_ACADEMIC_DATA, "SCHOOL", school.getId(),
                "name=" + normalizedName);
        return toSchoolResponse(school);
    }

    @Override
    @Transactional
    public AdminSchoolResponse updateSchool(Long schoolId, String name, String shortName) {
        CustomUserPrincipal principal = requireActiveAdmin();
        School school = requireSchoolForUpdate(schoolId);
        String normalizedName = normalizeRequiredName(name, MAX_ACADEMIC_NAME_LENGTH);
        String normalizedShortName = normalizeOptionalName(shortName, MAX_SHORT_NAME_LENGTH);
        if (Objects.equals(school.getName(), normalizedName)
                && Objects.equals(school.getShortName(), normalizedShortName)) {
            return toSchoolResponse(school);
        }
        if (schoolRepository.existsByNameAndIdNot(normalizedName, schoolId)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_SCHOOL_ALREADY_EXISTS);
        }
        String oldValue = school.getName();
        school.update(normalizedName, normalizedShortName);
        saveSchool(school);
        saveAudit(principal, AdminActionType.UPDATE_ACADEMIC_DATA, "SCHOOL", schoolId,
                "old=" + oldValue + "; new=" + normalizedName);
        return toSchoolResponse(school);
    }

    @Override
    @Transactional
    public AdminSchoolResponse changeSchoolStatus(Long schoolId, AcademicStatus status) {
        CustomUserPrincipal principal = requireActiveAdmin();
        School school = requireSchoolForUpdate(schoolId);
        AcademicStatus requiredStatus = requireStatus(status);
        if (school.getStatus() == requiredStatus) return toSchoolResponse(school);
        AcademicStatus oldStatus = school.getStatus();
        school.changeStatus(requiredStatus);
        schoolRepository.saveAndFlush(school);
        saveAudit(principal, AdminActionType.CHANGE_ACADEMIC_STATUS, "SCHOOL", schoolId,
                "old=" + oldStatus + "; new=" + requiredStatus);
        return toSchoolResponse(school);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminFacultyResponse> getFaculties(
            Long schoolId, String keyword, int page, int size
    ) {
        validatePagination(page, size);
        requirePositiveId(schoolId);
        if (!schoolRepository.existsById(schoolId)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_SCHOOL_NOT_FOUND);
        }
        return PageResponse.from(facultyRepository.searchForAdminBySchool(
                schoolId, normalizeKeyword(keyword), PageRequest.of(page, size)).map(this::toFacultyResponse));
    }

    @Override
    @Transactional
    public AdminFacultyResponse createFaculty(Long schoolId, String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        requirePositiveId(schoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACADEMIC_SCHOOL_NOT_FOUND));
        String normalizedName = normalizeRequiredName(name, MAX_ACADEMIC_NAME_LENGTH);
        if (facultyRepository.existsBySchoolIdAndName(schoolId, normalizedName)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_FACULTY_ALREADY_EXISTS);
        }
        Faculty faculty = saveFaculty(new Faculty(school, normalizedName));
        saveAudit(principal, AdminActionType.CREATE_ACADEMIC_DATA, "FACULTY", faculty.getId(),
                "schoolId=" + schoolId + "; name=" + normalizedName);
        return toFacultyResponse(faculty);
    }

    @Override
    @Transactional
    public AdminFacultyResponse updateFaculty(Long facultyId, String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        Faculty faculty = requireFacultyForUpdate(facultyId);
        String normalizedName = normalizeRequiredName(name, MAX_ACADEMIC_NAME_LENGTH);
        if (Objects.equals(faculty.getName(), normalizedName)) return toFacultyResponse(faculty);
        Long schoolId = faculty.getSchool().getId();
        if (facultyRepository.existsBySchoolIdAndNameAndIdNot(schoolId, normalizedName, facultyId)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_FACULTY_ALREADY_EXISTS);
        }
        String oldValue = faculty.getName();
        faculty.updateName(normalizedName);
        saveFaculty(faculty);
        saveAudit(principal, AdminActionType.UPDATE_ACADEMIC_DATA, "FACULTY", facultyId,
                "old=" + oldValue + "; new=" + normalizedName);
        return toFacultyResponse(faculty);
    }

    @Override
    @Transactional
    public AdminFacultyResponse changeFacultyStatus(Long facultyId, AcademicStatus status) {
        CustomUserPrincipal principal = requireActiveAdmin();
        Faculty faculty = requireFacultyForUpdate(facultyId);
        AcademicStatus requiredStatus = requireStatus(status);
        if (faculty.getStatus() == requiredStatus) return toFacultyResponse(faculty);
        AcademicStatus oldStatus = faculty.getStatus();
        faculty.changeStatus(requiredStatus);
        facultyRepository.saveAndFlush(faculty);
        saveAudit(principal, AdminActionType.CHANGE_ACADEMIC_STATUS, "FACULTY", facultyId,
                "old=" + oldStatus + "; new=" + requiredStatus);
        return toFacultyResponse(faculty);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminMajorResponse> getMajors(Long facultyId, String keyword, int page, int size) {
        validatePagination(page, size);
        requirePositiveId(facultyId);
        if (!facultyRepository.existsById(facultyId)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_FACULTY_NOT_FOUND);
        }
        return PageResponse.from(majorRepository.searchForAdminByFaculty(
                facultyId, normalizeKeyword(keyword), PageRequest.of(page, size)).map(this::toMajorResponse));
    }

    @Override
    @Transactional
    public AdminMajorResponse createMajor(Long facultyId, String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        requirePositiveId(facultyId);
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACADEMIC_FACULTY_NOT_FOUND));
        String normalizedName = normalizeRequiredName(name, MAX_ACADEMIC_NAME_LENGTH);
        if (majorRepository.existsByFacultyIdAndName(facultyId, normalizedName)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_MAJOR_ALREADY_EXISTS);
        }
        Major major = saveMajor(new Major(faculty, normalizedName));
        saveAudit(principal, AdminActionType.CREATE_ACADEMIC_DATA, "MAJOR", major.getId(),
                "facultyId=" + facultyId + "; name=" + normalizedName);
        return toMajorResponse(major);
    }

    @Override
    @Transactional
    public AdminMajorResponse updateMajor(Long majorId, String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        Major major = requireMajorForUpdate(majorId);
        String normalizedName = normalizeRequiredName(name, MAX_ACADEMIC_NAME_LENGTH);
        if (Objects.equals(major.getName(), normalizedName)) return toMajorResponse(major);
        Long facultyId = major.getFaculty().getId();
        if (majorRepository.existsByFacultyIdAndNameAndIdNot(facultyId, normalizedName, majorId)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_MAJOR_ALREADY_EXISTS);
        }
        String oldValue = major.getName();
        major.updateName(normalizedName);
        saveMajor(major);
        saveAudit(principal, AdminActionType.UPDATE_ACADEMIC_DATA, "MAJOR", majorId,
                "old=" + oldValue + "; new=" + normalizedName);
        return toMajorResponse(major);
    }

    @Override
    @Transactional
    public AdminMajorResponse changeMajorStatus(Long majorId, AcademicStatus status) {
        CustomUserPrincipal principal = requireActiveAdmin();
        Major major = requireMajorForUpdate(majorId);
        AcademicStatus requiredStatus = requireStatus(status);
        if (major.getStatus() == requiredStatus) return toMajorResponse(major);
        AcademicStatus oldStatus = major.getStatus();
        major.changeStatus(requiredStatus);
        majorRepository.saveAndFlush(major);
        saveAudit(principal, AdminActionType.CHANGE_ACADEMIC_STATUS, "MAJOR", majorId,
                "old=" + oldStatus + "; new=" + requiredStatus);
        return toMajorResponse(major);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminInterestResponse> getInterests(String keyword, int page, int size) {
        validatePagination(page, size);
        return PageResponse.from(interestRepository.searchForAdmin(
                normalizeKeyword(keyword), PageRequest.of(page, size)).map(this::toInterestResponse));
    }

    @Override
    @Transactional
    public AdminInterestResponse createInterest(String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        String normalizedName = normalizeRequiredName(name, MAX_INTEREST_NAME_LENGTH);
        if (interestRepository.existsByName(normalizedName)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_INTEREST_ALREADY_EXISTS);
        }
        InterestCategory interest = saveInterest(new InterestCategory(normalizedName));
        saveAudit(principal, AdminActionType.CREATE_ACADEMIC_DATA, "INTEREST", interest.getId(),
                "name=" + normalizedName);
        return toInterestResponse(interest);
    }

    @Override
    @Transactional
    public AdminInterestResponse updateInterest(Long interestId, String name) {
        CustomUserPrincipal principal = requireActiveAdmin();
        InterestCategory interest = requireInterestForUpdate(interestId);
        String normalizedName = normalizeRequiredName(name, MAX_INTEREST_NAME_LENGTH);
        if (Objects.equals(interest.getName(), normalizedName)) return toInterestResponse(interest);
        if (interestRepository.existsByNameAndIdNot(normalizedName, interestId)) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_INTEREST_ALREADY_EXISTS);
        }
        String oldValue = interest.getName();
        interest.updateName(normalizedName);
        saveInterest(interest);
        saveAudit(principal, AdminActionType.UPDATE_ACADEMIC_DATA, "INTEREST", interestId,
                "old=" + oldValue + "; new=" + normalizedName);
        return toInterestResponse(interest);
    }

    @Override
    @Transactional
    public AdminInterestResponse changeInterestStatus(Long interestId, AcademicStatus status) {
        CustomUserPrincipal principal = requireActiveAdmin();
        InterestCategory interest = requireInterestForUpdate(interestId);
        AcademicStatus requiredStatus = requireStatus(status);
        if (interest.getStatus() == requiredStatus) return toInterestResponse(interest);
        AcademicStatus oldStatus = interest.getStatus();
        interest.changeStatus(requiredStatus);
        interestRepository.saveAndFlush(interest);
        saveAudit(principal, AdminActionType.CHANGE_ACADEMIC_STATUS, "INTEREST", interestId,
                "old=" + oldStatus + "; new=" + requiredStatus);
        return toInterestResponse(interest);
    }

    private School saveSchool(School school) {
        try {
            schoolRepository.saveAndFlush(school);
            entityManager.refresh(school);
            return school;
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_SCHOOL_ALREADY_EXISTS);
        }
    }

    private Faculty saveFaculty(Faculty faculty) {
        try {
            facultyRepository.saveAndFlush(faculty);
            entityManager.refresh(faculty);
            return faculty;
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_FACULTY_ALREADY_EXISTS);
        }
    }

    private Major saveMajor(Major major) {
        try {
            majorRepository.saveAndFlush(major);
            entityManager.refresh(major);
            return major;
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_MAJOR_ALREADY_EXISTS);
        }
    }

    private InterestCategory saveInterest(InterestCategory interest) {
        try {
            interestRepository.saveAndFlush(interest);
            entityManager.refresh(interest);
            return interest;
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_INTEREST_ALREADY_EXISTS);
        }
    }

    private School requireSchoolForUpdate(Long id) {
        requirePositiveId(id);
        return schoolRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACADEMIC_SCHOOL_NOT_FOUND));
    }

    private Faculty requireFacultyForUpdate(Long id) {
        requirePositiveId(id);
        return facultyRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACADEMIC_FACULTY_NOT_FOUND));
    }

    private Major requireMajorForUpdate(Long id) {
        requirePositiveId(id);
        return majorRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACADEMIC_MAJOR_NOT_FOUND));
    }

    private InterestCategory requireInterestForUpdate(Long id) {
        requirePositiveId(id);
        return interestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACADEMIC_INTEREST_NOT_FOUND));
    }

    private CustomUserPrincipal requireActiveAdmin() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.ADMIN) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (principal.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.USER_BLOCKED);
        return principal;
    }

    private void saveAudit(
            CustomUserPrincipal principal, AdminActionType actionType, String kind, Long targetId, String note
    ) {
        User admin = entityManager.getReference(User.class, principal.getUserId());
        adminActionRepository.save(new AdminAction(
                admin, actionType, AdminTargetType.ACADEMIC_DATA, targetId, "type=" + kind + "; " + note));
    }

    private AcademicStatus requireStatus(AcademicStatus status) {
        if (status == null) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        return status;
    }

    private String normalizeRequiredName(String value, int maxCodePoints) {
        String normalized = normalizeOptionalName(value, maxCodePoints);
        if (normalized == null) throw new BusinessException(ErrorCode.ADMIN_ACADEMIC_NAME_REQUIRED);
        return normalized;
    }

    private String normalizeOptionalName(String value, int maxCodePoints) {
        if (value == null || value.isBlank()) return null;
        String normalized = Normalizer.normalize(value.strip().replaceAll("(?U)\\s+", " "), Normalizer.Form.NFC);
        if (normalized.codePointCount(0, normalized.length()) > maxCodePoints) {
            throw new BusinessException(maxCodePoints == MAX_SHORT_NAME_LENGTH
                    ? ErrorCode.ADMIN_ACADEMIC_SHORT_NAME_TOO_LONG
                    : ErrorCode.ADMIN_ACADEMIC_NAME_TOO_LONG);
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return "";
        String normalized = keyword.strip();
        if (normalized.codePointCount(0, normalized.length()) > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.ACADEMIC_KEYWORD_TOO_LONG);
        }
        return LikePatternEscaper.escape(normalized);
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    private AdminSchoolResponse toSchoolResponse(School school) {
        return new AdminSchoolResponse(school.getId(), school.getName(), school.getShortName(), school.getStatus(),
                school.getCreatedAt(), school.getUpdatedAt());
    }

    private AdminFacultyResponse toFacultyResponse(Faculty faculty) {
        return new AdminFacultyResponse(faculty.getId(), faculty.getSchool().getId(), faculty.getName(),
                faculty.getStatus(), faculty.getCreatedAt(), faculty.getUpdatedAt());
    }

    private AdminMajorResponse toMajorResponse(Major major) {
        return new AdminMajorResponse(major.getId(), major.getFaculty().getId(), major.getName(), major.getStatus(),
                major.getCreatedAt(), major.getUpdatedAt());
    }

    private AdminInterestResponse toInterestResponse(InterestCategory interest) {
        return new AdminInterestResponse(interest.getId(), interest.getName(), interest.getStatus(),
                interest.getCreatedAt(), interest.getUpdatedAt());
    }
}
