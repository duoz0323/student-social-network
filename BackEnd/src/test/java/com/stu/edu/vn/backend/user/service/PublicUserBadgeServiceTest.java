package com.stu.edu.vn.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.collaborator.identity.AdminSocialIdentityRepository;
import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import java.util.List;
import org.junit.jupiter.api.Test;

class PublicUserBadgeServiceTest {
    @Test
    void onlyActiveCollaboratorIdentityReceivesBadgeWhileNormalAndDisabledManagedDoNot() {
        AdminSocialIdentityRepository identities = mock(AdminSocialIdentityRepository.class);
        when(identities.findActiveCollaboratorSocialUserIds(List.of(10L, 20L, 30L))).thenReturn(List.of(20L));
        var badges = new PublicUserBadgeService(identities).getBadgesByUserIds(List.of(10L, 20L, 30L));
        assertThat(badges.get(10L)).isEmpty();
        assertThat(badges.get(20L)).containsExactly(PublicUserBadge.COLLABORATOR);
        assertThat(badges.get(30L)).isEmpty();
    }
}
