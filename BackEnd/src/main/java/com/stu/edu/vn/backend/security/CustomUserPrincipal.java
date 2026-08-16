package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.admin.rbac.AdminAuthorities;
import com.stu.edu.vn.backend.admin.rbac.AdminPermission;
import com.stu.edu.vn.backend.admin.rbac.AdminRoleCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Principal tối giản lưu trong SecurityContext để các API /me lấy đúng người dùng từ Access Token.
 */
@Getter
public class CustomUserPrincipal implements AuthenticatedPrincipal {

    private final Long userId;
    private final UserRole role;
    private final UserStatus status;
    private final Set<String> adminRoles;
    private final Set<String> permissions;

    public CustomUserPrincipal(Long userId, UserRole role, UserStatus status) {
        this(userId, role, status, Set.of(), Set.of());
    }

    /** Token ADMIN cũ được giữ toàn quyền trong thời gian Access Token ngắn hạn còn hiệu lực. */
    public static CustomUserPrincipal legacyAdmin(Long userId, UserStatus status) {
        Set<String> allPermissions = java.util.Arrays.stream(AdminPermission.values()).map(Enum::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new CustomUserPrincipal(userId, UserRole.ADMIN, status,
                Set.of(AdminRoleCode.SUPER_ADMIN.name()), allPermissions);
    }

    public CustomUserPrincipal(
            Long userId,
            UserRole role,
            UserStatus status,
            Set<String> adminRoles,
            Set<String> permissions
    ) {
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.adminRoles = adminRoles == null ? Set.of() : Set.copyOf(adminRoles);
        this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> codes = new LinkedHashSet<>();
        codes.add("ROLE_" + role.name());
        adminRoles.forEach(code -> codes.add(AdminAuthorities.ADMIN_ROLE_PREFIX + code));
        codes.addAll(permissions);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        codes.forEach(code -> authorities.add(new SimpleGrantedAuthority(code)));
        return authorities;
    }

    /**
     * User destination của STOMP dùng tên ổn định này thay vì chuỗi toString phụ thuộc instance.
     */
    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
