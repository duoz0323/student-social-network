package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import java.time.LocalDateTime;

/** Kết quả nội bộ giữ raw token đủ lâu để trả Client đúng một lần. */
public record ReauthenticationChallengeCreation(
        String rawToken,
        ReauthenticationMethod method,
        ReauthenticationScope purpose,
        AuthMethod targetMethod,
        LocalDateTime expiresAt,
        ReauthenticationChallengeStatus status
) {
}
