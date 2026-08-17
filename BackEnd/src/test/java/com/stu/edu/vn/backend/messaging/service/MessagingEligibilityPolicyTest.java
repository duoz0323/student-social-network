package com.stu.edu.vn.backend.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MessagingEligibilityPolicyTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final MessagingEligibilityPolicy policy = new MessagingEligibilityPolicy(users, profiles);

    @Test
    void normalCompletedUserIsEligible() {
        User user = new User("normal@example.com", "hash");
        when(users.findById(10L)).thenReturn(Optional.of(user));
        when(profiles.existsByUserIdAndProfileCompletedAtIsNotNull(10L)).thenReturn(true);
        assertThat(policy.requireEligible(10L)).isSameAs(user);
    }

    @Test
    void managedAccountIsRejectedEvenWhenActiveAndCompleted() {
        User managed = new User(null, null);
        managed.setAccountType(UserAccountType.MANAGED);
        when(users.findById(20L)).thenReturn(Optional.of(managed));
        when(profiles.existsByUserIdAndProfileCompletedAtIsNotNull(20L)).thenReturn(true);

        assertThatThrownBy(() -> policy.requireEligible(20L))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.MESSAGING_NOT_ALLOWED);
    }
}
