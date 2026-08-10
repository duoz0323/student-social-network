package com.stu.edu.vn.backend.academic.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.academic.repository.FacultyRepository;
import com.stu.edu.vn.backend.academic.repository.InterestCategoryRepository;
import com.stu.edu.vn.backend.academic.repository.MajorRepository;
import com.stu.edu.vn.backend.academic.repository.SchoolRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

/** Kiểm chứng giới hạn và chuẩn hóa truy vấn autocomplete Academic. */
class AcademicCatalogServiceImplTest {
    private final SchoolRepository schools = org.mockito.Mockito.mock(SchoolRepository.class);
    private final FacultyRepository faculties = org.mockito.Mockito.mock(FacultyRepository.class);
    private final MajorRepository majors = org.mockito.Mockito.mock(MajorRepository.class);
    private final InterestCategoryRepository interests = org.mockito.Mockito.mock(InterestCategoryRepository.class);
    private AcademicCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AcademicCatalogServiceImpl(schools, faculties, majors, interests);
    }

    @Test
    void schoolAutocompleteUsesActiveStatusEscapedPrefixAndDefaultLimit() {
        when(schools.searchActive(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(List.of());

        service.searchSchools("  Công_%  ", null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(schools).searchActive(
                org.mockito.ArgumentMatchers.eq(AcademicStatus.ACTIVE),
                org.mockito.ArgumentMatchers.eq("Công=_=%"),
                pageable.capture()
        );
        org.assertj.core.api.Assertions.assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void rejectsLimitAboveAutocompleteMaximum() {
        assertThatThrownBy(() -> service.searchSchools(null, 21))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACADEMIC_LIMIT_INVALID);
    }
}
