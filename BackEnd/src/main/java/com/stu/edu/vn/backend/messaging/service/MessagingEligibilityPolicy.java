package com.stu.edu.vn.backend.messaging.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Một nguồn policy duy nhất: chỉ NORMAL USER active đã onboarding được tham gia Messaging. */
@Service
@RequiredArgsConstructor
public class MessagingEligibilityPolicy {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public User requireEligible(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGING_NOT_ALLOWED));
        if (user.getRole() != UserRole.USER || user.getStatus() != UserStatus.ACTIVE
                || user.getAccountType() != UserAccountType.NORMAL
                || !profileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(userId)) {
            throw new BusinessException(ErrorCode.MESSAGING_NOT_ALLOWED);
        }
        return user;
    }
}
