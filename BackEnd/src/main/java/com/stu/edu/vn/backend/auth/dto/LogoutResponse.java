package com.stu.edu.vn.backend.auth.dto;

/**
 * Response đăng xuất tối giản, không trả token thô hoặc token hash.
 */
public record LogoutResponse(
        boolean loggedOut
) {
}
