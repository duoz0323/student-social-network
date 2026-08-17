package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.repository.ModerationCaseRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.dto.response.AccountStandingResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cung cấp Account Standing từ Moderation Case, không dùng report_count hoặc state Frontend. */
@Service
public class AccountStandingService {
    static final long VIOLATION_THRESHOLD = 3L;

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final ModerationCaseRepository moderationCaseRepository;

    public AccountStandingService(CurrentUserProvider currentUserProvider, UserRepository userRepository,
            ModerationCaseRepository moderationCaseRepository) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.moderationCaseRepository = moderationCaseRepository;
    }

    @Transactional(readOnly = true)
    public AccountStandingResponse getCurrentStanding() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.USER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        long count = moderationCaseRepository.countByPost_Author_IdAndStatus(
                user.getId(), ModerationCaseStatus.RESOLVED_ACTION_TAKEN);
        return new AccountStandingResponse(user.getStatus(), count, VIOLATION_THRESHOLD,
                Math.max(0L, VIOLATION_THRESHOLD - count));
    }
}
