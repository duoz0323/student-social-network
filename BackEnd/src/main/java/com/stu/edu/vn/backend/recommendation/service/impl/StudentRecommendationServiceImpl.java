package com.stu.edu.vn.backend.recommendation.service.impl;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.recommendation.dto.response.StudentRecommendationResponse;
import com.stu.edu.vn.backend.recommendation.mapper.StudentRecommendationMapper;
import com.stu.edu.vn.backend.recommendation.repository.StudentRecommendationProjection;
import com.stu.edu.vn.backend.recommendation.repository.StudentRecommendationRepository;
import com.stu.edu.vn.backend.recommendation.service.StudentRecommendationService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Điều phối Recommendation V1 cho đúng USER hiện tại; query không nhận tiêu chí do Client tự khai báo. */
@Service
public class StudentRecommendationServiceImpl implements StudentRecommendationService {

    private final CurrentUserProvider currentUserProvider;
    private final UserProfileRepository userProfileRepository;
    private final StudentRecommendationRepository recommendationRepository;
    private final StudentRecommendationMapper recommendationMapper;

    public StudentRecommendationServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserProfileRepository userProfileRepository,
            StudentRecommendationRepository recommendationRepository,
            StudentRecommendationMapper recommendationMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userProfileRepository = userProfileRepository;
        this.recommendationRepository = recommendationRepository;
        this.recommendationMapper = recommendationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentRecommendationResponse> getStudentRecommendations(int page, int size) {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        ensureEligibleCurrentUser(principal);
        Page<StudentRecommendationProjection> recommendations = recommendationRepository.findStudentRecommendations(
                principal.getUserId(), PageRequest.of(page, size));
        return PageResponse.from(recommendations.map(recommendationMapper::toResponse));
    }

    private void ensureEligibleCurrentUser(CustomUserPrincipal principal) {
        if (principal.getRole() != UserRole.USER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (principal.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        if (!userProfileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(principal.getUserId())) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
    }
}
