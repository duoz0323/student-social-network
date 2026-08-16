package com.stu.edu.vn.backend.security;

import java.util.Set;

/** Snapshot role/quyền được ký trong Access Token. */
public record AdminAuthorization(Set<String> roles, Set<String> permissions) {
    public AdminAuthorization {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
