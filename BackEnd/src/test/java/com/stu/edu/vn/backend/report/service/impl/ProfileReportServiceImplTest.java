package com.stu.edu.vn.backend.report.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.report.dto.request.CreateProfileReportRequest;
import com.stu.edu.vn.backend.report.entity.ProfileReport;
import com.stu.edu.vn.backend.report.entity.ProfileReportCase;
import com.stu.edu.vn.backend.report.enums.ProfileReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.repository.ProfileReportRepository;
import com.stu.edu.vn.backend.report.repository.ProfileReportCaseRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ProfileReportServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository profileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final UserRelationshipPolicyService relationshipPolicy = org.mockito.Mockito.mock(UserRelationshipPolicyService.class);
    private final ProfileReportRepository reportRepository = org.mockito.Mockito.mock(ProfileReportRepository.class);
    private final ProfileReportCaseRepository caseRepository = org.mockito.Mockito.mock(ProfileReportCaseRepository.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private ProfileReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProfileReportServiceImpl(currentUserProvider, userRepository, profileRepository,
                relationshipPolicy, reportRepository, caseRepository, entityManager,
                Clock.fixed(Instant.parse("2026-08-09T03:00:00Z"), ZoneOffset.UTC));
        User reporter = user(10L);
        User target = user(20L);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(reporter));
        when(userRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(target));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile(reporter, "Reporter")));
        when(profileRepository.findById(20L)).thenReturn(Optional.of(profile(target, "Target")));
        when(relationshipPolicy.existsBlockEitherDirection(10L, 20L)).thenReturn(false);
        when(reportRepository.existsByReporter_IdAndReportedUser_IdAndStatus(10L, 20L, ReportStatus.PENDING))
                .thenReturn(false);
        when(caseRepository.findByReportedUser_Id(20L)).thenReturn(Optional.empty());
        when(reportRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            ProfileReport report = invocation.getArgument(0);
            ReflectionTestUtils.setField(report, "id", 99L);
            ReflectionTestUtils.setField(report, "createdAt", LocalDateTime.of(2026, 8, 9, 10, 0));
            return report;
        });
    }

    @Test
    void createsPendingReportWithProfileSnapshot() {
        var response = service.createProfileReport(
                20L, new CreateProfileReportRequest(ProfileReportReason.IMPERSONATION));

        ArgumentCaptor<ProfileReport> captor = ArgumentCaptor.forClass(ProfileReport.class);
        verify(reportRepository).saveAndFlush(captor.capture());
        assertThat(response.reportId()).isEqualTo(99L);
        assertThat(response.status()).isEqualTo(ReportStatus.PENDING);
        assertThat(captor.getValue().getReportedDisplayNameSnapshot()).isEqualTo("Target");
        assertThat(captor.getValue().getReporter().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getReportCase().getReportCount()).isEqualTo(1);
    }

    @Test
    void rejectsSelfReportBeforeDatabaseMutation() {
        assertThatThrownBy(() -> service.createProfileReport(
                10L, new CreateProfileReportRequest(ProfileReportReason.FALSE_INFORMATION)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROFILE_REPORT_SELF_FORBIDDEN));
        verify(reportRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsSecondPendingReportForSameProfile() {
        when(reportRepository.existsByReporter_IdAndReportedUser_IdAndStatus(10L, 20L, ReportStatus.PENDING))
                .thenReturn(true);
        assertThatThrownBy(() -> service.createProfileReport(
                20L, new CreateProfileReportRequest(ProfileReportReason.SCAM_OR_FRAUD)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROFILE_REPORT_ALREADY_PENDING));
    }

    private User user(Long id) {
        User user = new User("u" + id + "@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private UserProfile profile(User user, String displayName) {
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName(displayName);
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return profile;
    }
}
