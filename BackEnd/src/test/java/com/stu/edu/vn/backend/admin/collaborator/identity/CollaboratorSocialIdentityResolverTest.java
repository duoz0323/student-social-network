package com.stu.edu.vn.backend.admin.collaborator.identity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CollaboratorSocialIdentityResolverTest {
    @Test
    void missingIdentityReturnsDedicatedBusinessError() {
        AdminSocialIdentityRepository repository = mock(AdminSocialIdentityRepository.class);
        when(repository.findByAdminId(15L)).thenReturn(Optional.empty());
        CollaboratorSocialIdentityResolver resolver = new CollaboratorSocialIdentityResolver(repository);
        assertThatThrownBy(() -> resolver.resolveActive(15L))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> org.assertj.core.api.Assertions.assertThat(error.getErrorCode())
                                .isEqualTo(ErrorCode.COLLABORATOR_SOCIAL_IDENTITY_NOT_FOUND));
    }

    @Test
    void disabledIdentityCannotAct() {
        AdminSocialIdentityRepository repository = mock(AdminSocialIdentityRepository.class);
        AdminSocialIdentity identity = mock(AdminSocialIdentity.class);
        User managed = mock(User.class);
        when(repository.findByAdminId(15L)).thenReturn(Optional.of(identity));
        when(identity.getSocialUser()).thenReturn(managed);
        when(identity.getStatus()).thenReturn(ManagedSocialIdentityStatus.DISABLED);
        when(managed.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(managed.getAccountType()).thenReturn(UserAccountType.MANAGED);
        CollaboratorSocialIdentityResolver resolver = new CollaboratorSocialIdentityResolver(repository);
        assertThatThrownBy(() -> resolver.resolveActive(15L))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> org.assertj.core.api.Assertions.assertThat(error.getErrorCode())
                                .isEqualTo(ErrorCode.COLLABORATOR_SOCIAL_IDENTITY_DISABLED));
    }
}
