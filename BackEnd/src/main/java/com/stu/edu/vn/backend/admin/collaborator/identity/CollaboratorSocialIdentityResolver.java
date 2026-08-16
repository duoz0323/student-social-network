package com.stu.edu.vn.backend.admin.collaborator.identity;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Backend tự resolve actor từ admin trong JWT; controller không nhận socialUserId. */
@Component
@RequiredArgsConstructor
public class CollaboratorSocialIdentityResolver {
    private final AdminSocialIdentityRepository repository;

    @Transactional(readOnly = true)
    public User resolveActive(Long adminId) {
        AdminSocialIdentity identity = repository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLABORATOR_SOCIAL_IDENTITY_NOT_FOUND));
        User socialUser = identity.getSocialUser();
        if (identity.getStatus() != ManagedSocialIdentityStatus.ACTIVE
                || socialUser.getStatus() != UserStatus.ACTIVE
                || socialUser.getAccountType() != UserAccountType.MANAGED) {
            throw new BusinessException(ErrorCode.COLLABORATOR_SOCIAL_IDENTITY_DISABLED);
        }
        return socialUser;
    }
}
