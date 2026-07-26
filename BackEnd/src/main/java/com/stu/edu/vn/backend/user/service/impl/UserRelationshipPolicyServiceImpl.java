package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.repository.UserBlockRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cài đặt duy nhất của chính sách Block để tránh sai khác giữa các module. */
@Service
@RequiredArgsConstructor
public class UserRelationshipPolicyServiceImpl implements UserRelationshipPolicyService {
    private final UserBlockRepository userBlockRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsBlockEitherDirection(Long userAId, Long userBId) {
        return !userAId.equals(userBId) && userBlockRepository.existsEitherDirection(userAId, userBId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBlocked(Long blockerId, Long blockedId) {
        return userBlockRepository.existsByIdBlockerIdAndIdBlockedId(blockerId, blockedId);
    }

    @Override
    @Transactional(readOnly = true)
    public void assertNoBlock(Long userAId, Long userBId) {
        if (existsBlockEitherDirection(userAId, userBId)) {
            throw new BusinessException(ErrorCode.USER_RELATIONSHIP_BLOCKED);
        }
    }
}
