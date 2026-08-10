package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.dto.request.AcademicProfileRequest;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Kiểm chứng hierarchy, entry year và sở thích trước khi ghi Academic Profile. */
class AcademicProfileValidationSupportTest {
    private final SchoolRepository schools = org.mockito.Mockito.mock(SchoolRepository.class);
    private final FacultyRepository faculties = org.mockito.Mockito.mock(FacultyRepository.class);
    private final MajorRepository majors = org.mockito.Mockito.mock(MajorRepository.class);
    private final InterestCategoryRepository interests = org.mockito.Mockito.mock(InterestCategoryRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneOffset.UTC);
    private AcademicProfileValidationSupport support;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        support = new AcademicProfileValidationSupport(schools, faculties, majors, interests, clock);
        profile = new UserProfile(new User("student@example.com", "hash"));
    }

    @Test
    void acceptsActiveSchoolAndNullableLowerLevels() {
        School school = school(1L);
        when(schools.findByIdAndStatus(1L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(school));

        support.applyAcademic(profile, new AcademicProfileRequest(1L, null, null, 2026));

        assertThat(profile.getSchool()).isSameAs(school);
        assertThat(profile.getFaculty()).isNull();
        assertThat(profile.getMajor()).isNull();
    }

    @Test
    void acceptsFacultyBelongingToSelectedSchool() {
        School school = school(1L);
        Faculty faculty = faculty(2L, school);
        when(schools.findByIdAndStatus(1L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(school));
        when(faculties.findByIdAndStatus(2L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(faculty));

        support.applyAcademic(profile, new AcademicProfileRequest(1L, 2L, null, null));

        assertThat(profile.getFaculty()).isSameAs(faculty);
    }

    @Test
    void rejectsFacultyFromAnotherSchool() {
        School selectedSchool = school(1L);
        Faculty faculty = faculty(2L, school(9L));
        when(schools.findByIdAndStatus(1L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(selectedSchool));
        when(faculties.findByIdAndStatus(2L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(faculty));

        assertError(
                () -> support.applyAcademic(profile, new AcademicProfileRequest(1L, 2L, null, null)),
                ErrorCode.ACADEMIC_FACULTY_SCHOOL_MISMATCH
        );
    }

    @Test
    void acceptsMajorBelongingToSelectedFaculty() {
        School school = school(1L);
        Faculty faculty = faculty(2L, school);
        Major major = major(3L, faculty);
        stubHierarchy(school, faculty, major);

        support.applyAcademic(profile, new AcademicProfileRequest(1L, 2L, 3L, 2020));

        assertThat(profile.getMajor()).isSameAs(major);
    }

    @Test
    void rejectsMajorFromAnotherFaculty() {
        School school = school(1L);
        Faculty faculty = faculty(2L, school);
        Major major = major(3L, faculty(8L, school));
        stubHierarchy(school, faculty, major);

        assertError(
                () -> support.applyAcademic(profile, new AcademicProfileRequest(1L, 2L, 3L, null)),
                ErrorCode.ACADEMIC_MAJOR_FACULTY_MISMATCH
        );
    }

    @Test
    void rejectsFutureEntryYear() {
        assertError(
                () -> support.applyAcademic(profile, new AcademicProfileRequest(null, null, null, 2027)),
                ErrorCode.ACADEMIC_ENTRY_YEAR_INVALID
        );
    }

    @Test
    void duplicateInterestIdsCreateOnlyOneAssociation() {
        InterestCategory interest = interest(4L);
        when(interests.findAllByIdInAndStatus(org.mockito.ArgumentMatchers.anyCollection(),
                org.mockito.ArgumentMatchers.eq(AcademicStatus.ACTIVE))).thenReturn(List.of(interest));

        support.applyInterests(profile, List.of(4L, 4L));

        assertThat(profile.getInterests()).containsExactly(interest);
    }

    @Test
    void rejectsUnknownOrInactiveInterestId() {
        when(interests.findAllByIdInAndStatus(org.mockito.ArgumentMatchers.anyCollection(),
                org.mockito.ArgumentMatchers.eq(AcademicStatus.ACTIVE))).thenReturn(List.of());

        assertError(() -> support.applyInterests(profile, List.of(99L)), ErrorCode.INTEREST_INVALID);
    }

    @Test
    void nullableAcademicFieldsAndEmptyInterestsRemainValid() {
        support.applyAcademic(profile, new AcademicProfileRequest(null, null, null, null));
        support.applyInterests(profile, List.of());

        assertThat(profile.getSchool()).isNull();
        assertThat(profile.getInterests()).isEmpty();
    }

    private void stubHierarchy(School school, Faculty faculty, Major major) {
        when(schools.findByIdAndStatus(1L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(school));
        when(faculties.findByIdAndStatus(2L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(faculty));
        when(majors.findByIdAndStatus(3L, AcademicStatus.ACTIVE)).thenReturn(Optional.of(major));
    }

    private School school(Long id) {
        School value = org.mockito.Mockito.mock(School.class);
        when(value.getId()).thenReturn(id);
        return value;
    }

    private Faculty faculty(Long id, School school) {
        Faculty value = org.mockito.Mockito.mock(Faculty.class);
        when(value.getId()).thenReturn(id);
        when(value.getSchool()).thenReturn(school);
        return value;
    }

    private Major major(Long id, Faculty faculty) {
        Major value = org.mockito.Mockito.mock(Major.class);
        when(value.getId()).thenReturn(id);
        when(value.getFaculty()).thenReturn(faculty);
        return value;
    }

    private InterestCategory interest(Long id) {
        InterestCategory value = org.mockito.Mockito.mock(InterestCategory.class);
        when(value.getId()).thenReturn(id);
        return value;
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
