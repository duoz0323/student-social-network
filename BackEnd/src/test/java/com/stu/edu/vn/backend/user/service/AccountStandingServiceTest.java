package com.stu.edu.vn.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.repository.ModerationCaseRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AccountStandingServiceTest {
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ModerationCaseRepository moderationCaseRepository = mock(ModerationCaseRepository.class);
    private final CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
    private final AccountStandingService service = new AccountStandingService(
            currentUserProvider, userRepository, moderationCaseRepository);

    @BeforeEach
    void setUp() {
        when(currentUserProvider.getCurrentUser()).thenReturn(principal);
        when(principal.getUserId()).thenReturn(7L);
        when(principal.getRole()).thenReturn(UserRole.USER);
    }

    @Test
    void returnsAuthoritativeStandingFromResolvedActionCases() {
        User user = new User("student@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(moderationCaseRepository.countByPost_Author_IdAndStatus(
                7L, ModerationCaseStatus.RESOLVED_ACTION_TAKEN)).thenReturn(2L);

        var response = service.getCurrentStanding();

        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.confirmedViolationCount()).isEqualTo(2L);
        assertThat(response.violationThreshold()).isEqualTo(3L);
        assertThat(response.remainingBeforeBlock()).isEqualTo(1L);
    }

    @Test
    void clampsRemainingCountWhenHistoricalViolationsExceedThreshold() {
        User user = new User("student@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(moderationCaseRepository.countByPost_Author_IdAndStatus(
                7L, ModerationCaseStatus.RESOLVED_ACTION_TAKEN)).thenReturn(4L);

        assertThat(service.getCurrentStanding().remainingBeforeBlock()).isZero();
    }

    @Test
    void rejectsAdminBecauseStandingIsAUserSelfServiceEndpoint() {
        when(principal.getRole()).thenReturn(UserRole.ADMIN);

        assertThatThrownBy(service::getCurrentStanding)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
