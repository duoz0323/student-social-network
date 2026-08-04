package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserPairLockCoordinator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Khóa low ID trước high ID bằng pessimistic write lock trong transaction của caller. */
@Service
@RequiredArgsConstructor
public class UserPairLockCoordinatorImpl implements UserPairLockCoordinator {
    private final UserRepository userRepository;

    @Override
    public void lockPair(Long firstUserId, Long secondUserId) {
        long lowId = Math.min(firstUserId, secondUserId);
        long highId = Math.max(firstUserId, secondUserId);
        if (userRepository.findPairForUpdate(List.of(lowId, highId)).size() != 2) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
