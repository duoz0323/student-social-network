package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.*;

public interface PasswordRecoveryService {
    PasswordRecoveryChallengeResponse start(StartPasswordRecoveryRequest request);
    VerifyPasswordRecoveryResponse verify(String flowToken, VerifyPasswordRecoveryRequest request);
    PasswordRecoveryChallengeResponse resend(String flowToken);
    CompletePasswordRecoveryResponse complete(String resetToken, CompletePasswordRecoveryRequest request);
}
