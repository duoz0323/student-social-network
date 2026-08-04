package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.follow.repository.FollowRepository;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.dto.response.BlockedUserResponse;
import com.stu.edu.vn.backend.user.dto.response.UserBlockStatusResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserBlockRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.repository.UserRestrictionRepository;
import com.stu.edu.vn.backend.user.service.UserBlockService;
import com.stu.edu.vn.backend.user.service.UserPairLockCoordinator;
import lombok.RequiredArgsConstructor;
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
    private final UserRestrictionRepository userRestrictionRepository;
    private final UserPairLockCoordinator userPairLockCoordinator;

    @Override
    @Transactional
    public UserBlockStatusResponse block(Long targetUserId) {
        Long blockerId = currentUserProvider.getCurrentUserId();
        if (blockerId.equals(targetUserId)) throw new BusinessException(ErrorCode.CANNOT_BLOCK_SELF);
        userPairLockCoordinator.lockPair(blockerId, targetUserId);
        requireActiveUser(blockerId);
        requireActiveUser(targetUserId);
        // Upsert no-op chỉ xử lý composite PK trùng; lỗi FK/CHECK vẫn phải rollback thay vì bị che thành warning.
        userBlockRepository.insertIfAbsent(blockerId, targetUserId);
        // Block ưu tiên hơn Restrict nhưng chỉ xóa thiết lập cùng chiều của người đang Block.
        userRestrictionRepository.deleteRestriction(blockerId, targetUserId);
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
        // DELETE trả 0 dòng vẫn là kết quả thành công, đúng contract Unblock idempotent.
        userBlockRepository.deleteBlock(blockerId, targetUserId);
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
