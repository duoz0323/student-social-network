package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.response.BlockedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserBlockStatusResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserBlock;
import com.stu.edu.vn.backend.user.entity.UserBlockId;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserBlockRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Điều phối Block và hủy Follow hai chiều trong cùng một transaction. */
@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements UserBlockService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final FollowRepository followRepository;

    @Override
    @Transactional
    public UserBlockStatusResponse block(Long targetUserId) {
        Long blockerId = currentUserProvider.getCurrentUserId();
        if (blockerId.equals(targetUserId)) throw new BusinessException(ErrorCode.CANNOT_BLOCK_SELF);
        User blocker = requireActiveUser(blockerId);
        User blocked = requireActiveUser(targetUserId);
        if (!userBlockRepository.existsByIdBlockerIdAndIdBlockedId(blockerId, targetUserId)) {
            try {
                // Unique composite key bảo vệ hai request Block đồng thời.
                userBlockRepository.saveAndFlush(new UserBlock(blocker, blocked));
            } catch (DataIntegrityViolationException exception) {
                if (!userBlockRepository.existsByIdBlockerIdAndIdBlockedId(blockerId, targetUserId)) throw exception;
            }
        }
        // Hai phép xóa idempotent nằm cùng transaction với việc tạo Block.
        followRepository.deleteFollow(blockerId, targetUserId);
        followRepository.deleteFollow(targetUserId, blockerId);
        return new UserBlockStatusResponse(targetUserId, true);
    }

    @Override
    @Transactional
    public UserBlockStatusResponse unblock(Long targetUserId) {
        Long blockerId = currentUserProvider.getCurrentUserId();
        if (blockerId.equals(targetUserId)) throw new BusinessException(ErrorCode.CANNOT_BLOCK_SELF);
        userBlockRepository.deleteById(new UserBlockId(blockerId, targetUserId));
        return new UserBlockStatusResponse(targetUserId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlockedUserResponse> getMyBlockedUsers(int page, int size) {
        Long blockerId = currentUserProvider.getCurrentUserId();
        requireActiveUser(blockerId);
        if (page < 0 || size < 1 || size > 100) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        return PageResponse.from(userBlockRepository.findBlockedUsers(blockerId, PageRequest.of(page, size))
                .map(item -> new BlockedUserResponse(item.getUserId(), item.getDisplayName(),
                        item.getAvatarUrl(), item.getBlockedAt())));
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        return user;
    }
}
