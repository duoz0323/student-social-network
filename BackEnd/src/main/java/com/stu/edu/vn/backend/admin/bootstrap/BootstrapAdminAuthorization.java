package com.stu.edu.vn.backend.admin.bootstrap;

import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Xác minh các thao tác phân quyền chỉ do đúng tài khoản Bootstrap thực hiện. */
@Component("bootstrapAdminAuthorization")
@RequiredArgsConstructor
public class BootstrapAdminAuthorization {

    private final BootstrapAdminProperties properties;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    public boolean isCurrentBootstrapAdmin() {
        String configuredEmail;
        try {
            configuredEmail = EmailNormalizer.normalize(properties.getEmail()).value();
        } catch (BusinessException exception) {
            return false;
        }

        return userRepository.findById(currentUserProvider.getCurrentUserId())
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .map(user -> configuredEmail.equals(user.getEmail()))
                .orElse(false);
    }
}
