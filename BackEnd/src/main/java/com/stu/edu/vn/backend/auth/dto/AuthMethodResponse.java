package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.EmailLoginState;
import java.time.LocalDateTime;

/** Trạng thái phương thức đăng nhập an toàn, không chứa provider subject hay secret. */
public record AuthMethodResponse(
        AuthMethod type,
        String maskedIdentifier,
        boolean linked,
        boolean verified,
        LocalDateTime linkedAt,
        boolean canLink,
        boolean canUnlink,
        boolean passwordConfigured,
        boolean localLoginAvailable,
        EmailLoginState state,
        boolean canSetPassword,
        boolean canChangePassword
) {
    /** Constructor tương thích cho response social nội bộ cũ. */
    public AuthMethodResponse(AuthMethod type, String maskedIdentifier, boolean verified,
            LocalDateTime linkedAt, boolean canUnlink, boolean localLoginAvailable) {
        this(type, maskedIdentifier, true, verified, linkedAt, false, canUnlink,
                localLoginAvailable, localLoginAvailable,
                type == AuthMethod.EMAIL
                        ? (localLoginAvailable ? EmailLoginState.READY : EmailLoginState.VERIFIED_NO_PASSWORD)
                        : null,
                type == AuthMethod.EMAIL && verified && !localLoginAvailable,
                type == AuthMethod.EMAIL && localLoginAvailable);
    }
}
