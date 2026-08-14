package com.stu.edu.vn.backend.recommendation.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.recommendation.mapper.StudentRecommendationMapper;
import com.stu.edu.vn.backend.recommendation.repository.StudentRecommendationProjection;
import com.stu.edu.vn.backend.recommendation.repository.StudentRecommendationRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class StudentRecommendationServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final StudentRecommendationRepository repository = org.mockito.Mockito.mock(StudentRecommendationRepository.class);
    private final StudentRecommendationMapper mapper = org.mockito.Mockito.mock(StudentRecommendationMapper.class);
    private StudentRecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StudentRecommendationServiceImpl(currentUserProvider, userProfileRepository, repository, mapper);
    }

    @Test
    void eligibleUserUsesJwtIdentityAndPreservesPageMetadata() {
        CustomUserPrincipal principal = principal(10L, UserRole.USER, UserStatus.ACTIVE);
        StudentRecommendationProjection projection = org.mockito.Mockito.mock(StudentRecommendationProjection.class);
        when(currentUserProvider.getCurrentUser()).thenReturn(principal);
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(true);
        when(repository.findStudentRecommendations(10L, PageRequest.of(1, 5)))
                .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(1, 5), 8));

        var response = service.getStudentRecommendations(1, 5);

        verify(repository).findStudentRecommendations(10L, PageRequest.of(1, 5));
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(5);
        // PageImpl điều chỉnh total về offset + content khi trang mock là trang cuối.
        assertThat(response.totalElements()).isEqualTo(6);
    }

    @Test
    void rejectsAdminBlockedAndIncompleteUsersBeforeQuery() {
        when(currentUserProvider.getCurrentUser()).thenReturn(principal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        assertError(() -> service.getStudentRecommendations(0, 10), ErrorCode.FORBIDDEN);

        when(currentUserProvider.getCurrentUser()).thenReturn(principal(10L, UserRole.USER, UserStatus.BLOCKED));
        assertError(() -> service.getStudentRecommendations(0, 10), ErrorCode.USER_BLOCKED);

        when(currentUserProvider.getCurrentUser()).thenReturn(principal(10L, UserRole.USER, UserStatus.ACTIVE));
        when(userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(false);
        assertError(() -> service.getStudentRecommendations(0, 10), ErrorCode.PROFILE_NOT_COMPLETED);

        verify(repository, never()).findStudentRecommendations(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any());
    }

    private CustomUserPrincipal principal(Long id, UserRole role, UserStatus status) {
        return new CustomUserPrincipal(id, role, status);
    }

    private void assertError(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(code);
    }
}
