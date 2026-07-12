package com.stu.edu.vn.backend.follow.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.follow.dto.response.FollowStatusResponse;
import com.stu.edu.vn.backend.follow.dto.response.FollowUserResponse;
import com.stu.edu.vn.backend.follow.entity.Follow;
import com.stu.edu.vn.backend.follow.mapper.FollowMapper;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.follow.service.FollowService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai quản lý Follow tức thời, không có Follow Request, Block, Restrict hoặc Notification.
 */
@Service
public class FollowServiceImpl implements FollowService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final FollowMapper followMapper;

    public FollowServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            FollowRepository followRepository,
            FollowMapper followMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.followRepository = followRepository;
        this.followMapper = followMapper;
    }

    @Override
    @Transactional
    public FollowStatusResponse followUser(Long userId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User currentUser = ensureCurrentUserCanUseSocialFeatures(currentUserId);
        ensureNotSelf(currentUserId, userId);
        User targetUser = findActiveTargetUser(userId);

        if (followRepository.existsByIdFollowerIdAndIdFollowingId(currentUserId, userId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        try {
            // Flush ngay để primary key kép phát hiện hai request Follow đồng thời trong transaction hiện tại.
            followRepository.saveAndFlush(new Follow(currentUser, targetUser));
        } catch (DataIntegrityViolationException exception) {
            // Không để lộ tên constraint hoặc SQL; lỗi cạnh tranh được chuẩn hóa thành mã nghiệp vụ ổn định.
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }
        return new FollowStatusResponse(userId, true);
    }

    @Override
    @Transactional
    public FollowStatusResponse unfollowUser(Long userId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanUseSocialFeatures(currentUserId);
        ensureNotSelf(currentUserId, userId);

        // Không tải target để người dùng vẫn xóa được quan hệ cũ khi target đã BLOCKED.
        int deletedRows = followRepository.deleteFollow(currentUserId, userId);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.FOLLOW_NOT_FOUND);
        }
        return new FollowStatusResponse(userId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowers(Long userId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanUseSocialFeatures(currentUserId);
        ensureListOwnerIsActive(userId);

        return followRepository.findActiveFollowers(userId, currentUserId)
                .stream()
                .map(followMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowing(Long userId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanUseSocialFeatures(currentUserId);
        ensureListOwnerIsActive(userId);

        return followRepository.findActiveFollowing(userId, currentUserId)
                .stream()
                .map(followMapper::toResponse)
                .toList();
    }

    private User ensureCurrentUserCanUseSocialFeatures(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return currentUser;
    }

    private void ensureNotSelf(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_FORBIDDEN);
        }
    }

    private User findActiveTargetUser(Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (targetUser.getStatus() != UserStatus.ACTIVE) {
            // Target BLOCKED được coi là không khả dụng để không lộ trạng thái tài khoản nội bộ.
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return targetUser;
    }

    private void ensureListOwnerIsActive(Long userId) {
        findActiveTargetUser(userId);
    }
}
