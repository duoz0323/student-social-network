package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import java.time.LocalDateTime;

/** Trạng thái phương thức đăng nhập an toàn, không chứa provider subject hay secret. */
public record AuthMethodResponse(
        AuthMethod type,
        String maskedIdentifier,
        boolean verified,
        LocalDateTime linkedAt,
        boolean canUnlink,
        boolean localLoginAvailable
) { }
