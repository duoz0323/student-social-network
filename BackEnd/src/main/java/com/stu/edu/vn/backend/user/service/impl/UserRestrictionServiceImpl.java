package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.response.RestrictedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserRestrictionStatusResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRestrictionRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import com.stu.edu.vn.backend.user.service.UserRestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Điều phối Restrict một chiều mà không thay đổi Follow hoặc dữ liệu tương tác. */
@Service
@RequiredArgsConstructor
public class UserRestrictionServiceImpl implements UserRestrictionService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserRestrictionRepository restrictionRepository;
    private final UserRelationshipPolicyService relationshipPolicyService;

    @Override
    @Transactional
    public UserRestrictionStatusResponse restrict(Long targetUserId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_RESTRICT_SELF);
        }
        requireEligibleUser(currentUserId, false);
        requireEligibleUser(targetUserId, true);
        if (relationshipPolicyService.existsBlockEitherDirection(currentUserId, targetUserId)) {
            throw new BusinessException(ErrorCode.USER_RELATIONSHIP_BLOCKED);
        }
        restrictionRepository.insertIfAbsent(currentUserId, targetUserId);
        return new UserRestrictionStatusResponse(targetUserId, true);
    }

    @Override
    @Transactional
    public UserRestrictionStatusResponse unrestrict(Long targetUserId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_RESTRICT_SELF);
        }
        restrictionRepository.deleteRestriction(currentUserId, targetUserId);
        return new UserRestrictionStatusResponse(targetUserId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RestrictedUserResponse> getMyRestrictedUsers(int page, int size) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        requireEligibleUser(currentUserId, false);
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return PageResponse.from(restrictionRepository
                .findRestrictedUsers(currentUserId, PageRequest.of(page, size))
                .map(item -> new RestrictedUserResponse(item.getUserId(), item.getDisplayName(),
                        item.getAvatarUrl(), item.getRestrictedAt())));
    }

    private User requireEligibleUser(Long userId, boolean rejectAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE || (rejectAdmin && user.getRole() == UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
