package com.stu.edu.vn.backend.report.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.report.dto.request.CreateProfileReportRequest;
import com.stu.edu.vn.backend.report.dto.response.CreateProfileReportResponse;
import com.stu.edu.vn.backend.report.entity.ProfileReport;
import com.stu.edu.vn.backend.report.entity.ProfileReportCase;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.repository.ProfileReportRepository;
import com.stu.edu.vn.backend.report.repository.ProfileReportCaseRepository;
import com.stu.edu.vn.backend.report.service.ProfileReportService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tạo báo cáo hồ sơ với snapshot và chống trùng trong cùng transaction. */
@Service
public class ProfileReportServiceImpl implements ProfileReportService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRelationshipPolicyService relationshipPolicyService;
    private final ProfileReportRepository profileReportRepository;
    private final ProfileReportCaseRepository profileReportCaseRepository;
    private final EntityManager entityManager;
    private final Clock clock;

    public ProfileReportServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserRelationshipPolicyService relationshipPolicyService,
            ProfileReportRepository profileReportRepository,
            ProfileReportCaseRepository profileReportCaseRepository,
            EntityManager entityManager,
            Clock clock
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.relationshipPolicyService = relationshipPolicyService;
        this.profileReportRepository = profileReportRepository;
        this.profileReportCaseRepository = profileReportCaseRepository;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateProfileReportResponse createProfileReport(
            Long reportedUserId,
            CreateProfileReportRequest request
    ) {
        Long reporterId = currentUserProvider.getCurrentUserId();
        if (reportedUserId == null || reportedUserId <= 0 || request == null || request.reason() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (reporterId.equals(reportedUserId)) {
            throw new BusinessException(ErrorCode.PROFILE_REPORT_SELF_FORBIDDEN);
        }

        User reporter = requireActiveCompletedUser(reporterId);
        User reportedUser = userRepository.findByIdForUpdate(reportedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (reportedUser.getRole() != UserRole.USER || reportedUser.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }
        if (relationshipPolicyService.existsBlockEitherDirection(reporterId, reportedUserId)) {
            throw new BusinessException(ErrorCode.USER_RELATIONSHIP_BLOCKED);
        }

        UserProfile reporterProfile = requireCompletedProfile(reporterId);
        UserProfile reportedProfile = requireCompletedProfile(reportedUserId);
        if (profileReportRepository.existsByReporter_IdAndReportedUser_IdAndStatus(
                reporterId, reportedUserId, ReportStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PROFILE_REPORT_ALREADY_PENDING);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        ProfileReportCase reportCase = profileReportCaseRepository.findByReportedUser_Id(reportedUserId)
                .orElseGet(() -> new ProfileReportCase(reportedUser, reportedProfile, now));
        if (reportCase.getId() != null) {
            // Case cũ được mở lại khi xuất hiện lượt báo cáo mới.
            reportCase.registerReport(reportedProfile, now);
        }
        profileReportCaseRepository.save(reportCase);

        ProfileReport report = new ProfileReport(
                reportCase, reporter, reportedUser, request.reason(), reporterProfile, reportedProfile);
        try {
            profileReportRepository.saveAndFlush(report);
        } catch (DataIntegrityViolationException exception) {
            // Unique generated key biến hai request đồng thời thành cùng một lỗi nghiệp vụ ổn định.
            throw new BusinessException(ErrorCode.PROFILE_REPORT_ALREADY_PENDING);
        }
        entityManager.refresh(report);
        return new CreateProfileReportResponse(
                report.getId(), reportedUserId, report.getReason(), report.getStatus(), report.getCreatedAt());
    }

    private User requireActiveCompletedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        requireCompletedProfile(userId);
        return user;
    }

    private UserProfile requireCompletedProfile(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return profile;
    }
}
