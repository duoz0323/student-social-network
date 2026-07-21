package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictRequest;
import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictResponse;

/** Use case consume social conflict token một lần. */
public interface SocialConflictResolutionService {
    ResolveSocialConflictResponse resolve(
            String rawChallengeToken,
            ResolveSocialConflictRequest request,
            String ipAddress
    );
}
