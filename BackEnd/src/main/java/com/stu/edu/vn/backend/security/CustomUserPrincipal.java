package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Principal tối giản lưu trong SecurityContext để các API /me lấy đúng người dùng từ Access Token.
 */
public class CustomUserPrincipal {

    private final Long userId;
    private final UserRole role;
    private final UserStatus status;

    public CustomUserPrincipal(Long userId, UserRole role, UserStatus status) {
        this.userId = userId;
        this.role = role;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
