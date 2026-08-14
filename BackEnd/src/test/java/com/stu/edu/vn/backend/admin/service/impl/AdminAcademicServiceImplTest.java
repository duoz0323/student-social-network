package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.academic.entity.Faculty;
import com.stu.edu.vn.backend.academic.entity.InterestCategory;
import com.stu.edu.vn.backend.academic.entity.Major;
import com.stu.edu.vn.backend.academic.entity.School;
import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.academic.repository.FacultyRepository;
import com.stu.edu.vn.backend.academic.repository.InterestCategoryRepository;
import com.stu.edu.vn.backend.academic.repository.MajorRepository;
import com.stu.edu.vn.backend.academic.repository.SchoolRepository;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/** Kiểm chứng mutation, duplicate, audit và nguyên tắc bảo toàn reference của V1. */
class AdminAcademicServiceImplTest {
    private final SchoolRepository schools = org.mockito.Mockito.mock(SchoolRepository.class);
    private final FacultyRepository faculties = org.mockito.Mockito.mock(FacultyRepository.class);
    private final MajorRepository majors = org.mockito.Mockito.mock(MajorRepository.class);
    private final InterestCategoryRepository interests = org.mockito.Mockito.mock(InterestCategoryRepository.class);
    private final AdminActionRepository actions = org.mockito.Mockito.mock(AdminActionRepository.class);
    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private AdminAcademicServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminAcademicServiceImpl(
                schools, faculties, majors, interests, actions, currentUserProvider, entityManager);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(entityManager.getReference(User.class, 1L)).thenReturn(new User("admin@example.com", "hash"));
    }

    @Test
    void adminCreatesSchoolAndWritesAudit() {
        when(schools.existsByName("Đại học Công nghệ Sài Gòn")).thenReturn(false);
        when(schools.saveAndFlush(any(School.class))).thenAnswer(invocation -> {
            School school = invocation.getArgument(0);
            ReflectionTestUtils.setField(school, "id", 10L);
            return school;
        });

        var response = service.createSchool("  Đại học   Công nghệ Sài Gòn ", " STU ");

        assertThat(response.name()).isEqualTo("Đại học Công nghệ Sài Gòn");
        assertThat(response.shortName()).isEqualTo("STU");
        assertThat(response.status()).isEqualTo(AcademicStatus.ACTIVE);
        ArgumentCaptor<AdminAction> audit = ArgumentCaptor.forClass(AdminAction.class);
        verify(actions).save(audit.capture());
        assertThat(audit.getValue().getActionType()).isEqualTo(AdminActionType.CREATE_ACADEMIC_DATA);
        assertThat(audit.getValue().getTargetType()).isEqualTo(AdminTargetType.ACADEMIC_DATA);
    }

    @Test
    void duplicateSchoolIsRejected() {
        when(schools.existsByName("Trường A")).thenReturn(true);

        assertError(() -> service.createSchool("Trường A", null),
                ErrorCode.ADMIN_ACADEMIC_SCHOOL_ALREADY_EXISTS);
        verify(schools, never()).saveAndFlush(any());
    }

    @Test
    void adminUpdatesSchoolAndChangesStatusWithoutTouchingChildren() {
        School school = new School("Tên cũ", "OLD");
        ReflectionTestUtils.setField(school, "id", 10L);
        UserProfile existingProfile = new UserProfile(new User("student@example.com", "hash"));
        existingProfile.setSchool(school);
        when(schools.findByIdForUpdate(10L)).thenReturn(Optional.of(school));
        when(schools.existsByNameAndIdNot("Tên mới", 10L)).thenReturn(false);
        when(schools.saveAndFlush(school)).thenReturn(school);

        assertThat(service.updateSchool(10L, "Tên mới", "NEW").name()).isEqualTo("Tên mới");
        assertThat(service.changeSchoolStatus(10L, AcademicStatus.INACTIVE).status())
                .isEqualTo(AcademicStatus.INACTIVE);
        assertThat(existingProfile.getSchool()).isSameAs(school);
        verify(faculties, never()).saveAll(any());
        ArgumentCaptor<AdminAction> audit = ArgumentCaptor.forClass(AdminAction.class);
        verify(actions, times(2)).save(audit.capture());
        assertThat(audit.getAllValues().get(1).getActionType())
                .isEqualTo(AdminActionType.CHANGE_ACADEMIC_STATUS);
    }

    @Test
    void createAndUpdateFacultyRequireExistingSchoolAndRespectScopedDuplicate() {
        School school = new School("Trường A", "A");
        ReflectionTestUtils.setField(school, "id", 1L);
        when(schools.findById(1L)).thenReturn(Optional.of(school));
        when(faculties.saveAndFlush(any(Faculty.class))).thenAnswer(invocation -> {
            Faculty faculty = invocation.getArgument(0);
            ReflectionTestUtils.setField(faculty, "id", 2L);
            return faculty;
        });

        var created = service.createFaculty(1L, "Khoa CNTT");
        assertThat(created.schoolId()).isEqualTo(1L);
        Faculty faculty = new Faculty(school, "Khoa CNTT");
        ReflectionTestUtils.setField(faculty, "id", 2L);
        when(faculties.findByIdForUpdate(2L)).thenReturn(Optional.of(faculty));
        when(faculties.saveAndFlush(faculty)).thenReturn(faculty);
        assertThat(service.updateFaculty(2L, "Khoa Công nghệ thông tin").name())
                .isEqualTo("Khoa Công nghệ thông tin");

        assertError(() -> service.createFaculty(99L, "Khoa mới"),
                ErrorCode.ADMIN_ACADEMIC_SCHOOL_NOT_FOUND);
    }

    @Test
    void createAndUpdateMajorRequireExistingFaculty() {
        School school = new School("Trường A", "A");
        ReflectionTestUtils.setField(school, "id", 1L);
        Faculty faculty = new Faculty(school, "Khoa CNTT");
        ReflectionTestUtils.setField(faculty, "id", 2L);
        when(faculties.findById(2L)).thenReturn(Optional.of(faculty));
        when(majors.saveAndFlush(any(Major.class))).thenAnswer(invocation -> {
            Major major = invocation.getArgument(0);
            ReflectionTestUtils.setField(major, "id", 3L);
            return major;
        });

        assertThat(service.createMajor(2L, "Kỹ thuật phần mềm").facultyId()).isEqualTo(2L);
        Major major = new Major(faculty, "Kỹ thuật phần mềm");
        ReflectionTestUtils.setField(major, "id", 3L);
        when(majors.findByIdForUpdate(3L)).thenReturn(Optional.of(major));
        when(majors.saveAndFlush(major)).thenReturn(major);
        assertThat(service.updateMajor(3L, "Công nghệ phần mềm").name()).isEqualTo("Công nghệ phần mềm");
        assertError(() -> service.createMajor(99L, "Ngành mới"),
                ErrorCode.ADMIN_ACADEMIC_FACULTY_NOT_FOUND);
    }

    @Test
    void interestInactivePreservesExistingUserAssociation() {
        InterestCategory interest = new InterestCategory("Lập trình");
        ReflectionTestUtils.setField(interest, "id", 4L);
        UserProfile profile = new UserProfile(new User("student@example.com", "hash"));
        profile.getInterests().add(interest);
        when(interests.findByIdForUpdate(4L)).thenReturn(Optional.of(interest));
        when(interests.saveAndFlush(interest)).thenReturn(interest);

        assertThat(service.changeInterestStatus(4L, AcademicStatus.INACTIVE).status())
                .isEqualTo(AcademicStatus.INACTIVE);
        assertThat(profile.getInterests()).containsExactly(interest);
        verify(interests, never()).delete(any());
    }

    @Test
    void updateInterestAndMajorStatusAreSupported() {
        InterestCategory interest = new InterestCategory("Lập trình");
        ReflectionTestUtils.setField(interest, "id", 4L);
        when(interests.findByIdForUpdate(4L)).thenReturn(Optional.of(interest));
        when(interests.saveAndFlush(interest)).thenReturn(interest);
        assertThat(service.updateInterest(4L, "Công nghệ").name()).isEqualTo("Công nghệ");

        School school = new School("Trường A", "A");
        Faculty faculty = new Faculty(school, "Khoa A");
        Major major = new Major(faculty, "Ngành A");
        ReflectionTestUtils.setField(major, "id", 3L);
        when(majors.findByIdForUpdate(3L)).thenReturn(Optional.of(major));
        when(majors.saveAndFlush(major)).thenReturn(major);
        assertThat(service.changeMajorStatus(3L, AcademicStatus.INACTIVE).status())
                .isEqualTo(AcademicStatus.INACTIVE);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(errorCode);
    }
}
