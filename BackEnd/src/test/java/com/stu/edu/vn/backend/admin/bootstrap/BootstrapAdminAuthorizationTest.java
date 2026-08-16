package com.stu.edu.vn.backend.admin.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BootstrapAdminAuthorizationTest {

    @Test
    void onlyConfiguredBootstrapAdminCanDelegatePermissions() {
        BootstrapAdminProperties properties = new BootstrapAdminProperties();
        properties.setEmail("bootstrap@example.com");
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        UserRepository users = mock(UserRepository.class);
        User bootstrap = new User("bootstrap@example.com", "hash");
        bootstrap.setRole(UserRole.ADMIN);
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(users.findById(1L)).thenReturn(Optional.of(bootstrap));

        assertThat(new BootstrapAdminAuthorization(properties, currentUser, users)
                .isCurrentBootstrapAdmin()).isTrue();
    }

    @Test
    void anotherAdminCannotDelegateEvenWhenItHasAdminRole() {
        BootstrapAdminProperties properties = new BootstrapAdminProperties();
        properties.setEmail("bootstrap@example.com");
        CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
        UserRepository users = mock(UserRepository.class);
        User delegatedAdmin = new User("support@example.com", "hash");
        delegatedAdmin.setRole(UserRole.ADMIN);
        when(currentUser.getCurrentUserId()).thenReturn(2L);
        when(users.findById(2L)).thenReturn(Optional.of(delegatedAdmin));

        assertThat(new BootstrapAdminAuthorization(properties, currentUser, users)
                .isCurrentBootstrapAdmin()).isFalse();
    }
}
