package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import java.time.LocalDateTime;

/** Chỉ trả raw token một lần cùng phạm vi sử dụng; không trả Entity hoặc token hash. */
public record ReauthenticationResponse(
        String reauthenticationToken,
        ReauthenticationMethod method,
        ReauthenticationScope purpose,
        AuthMethod targetMethod,
        LocalDateTime expiresAt,
        ReauthenticationChallengeStatus status
) {
}
