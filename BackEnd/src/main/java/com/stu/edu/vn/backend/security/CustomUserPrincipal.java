package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Principal tối giản lưu trong SecurityContext để các API /me lấy đúng người dùng từ Access Token.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserPrincipal {

    private final Long userId;
    private final UserRole role;
    private final UserStatus status;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
