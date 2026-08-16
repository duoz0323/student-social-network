package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthResponse;
import com.stu.edu.vn.backend.auth.google.GoogleIdentityVerifier;
import com.stu.edu.vn.backend.auth.google.VerifiedGoogleIdentity;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Điều phối Google Auth; tuyệt đối không giữ transaction trong lúc verifier có thể tải public key. */
@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final GoogleIdentityVerifier identityVerifier;
    private final SocialAuthenticationTransactionService transactionService;

    public GoogleAuthServiceImpl(
            GoogleIdentityVerifier identityVerifier,
            SocialAuthenticationTransactionService transactionService
    ) {
        this.identityVerifier = identityVerifier;
        this.transactionService = transactionService;
    }

    @Override
    public GoogleAuthResponse authenticate(GoogleAuthRequest request, String ipAddress) {
        VerifiedGoogleIdentity identity = identityVerifier.verify(request.idToken());
        try {
            return map(transactionService.authenticate(AuthProvider.GOOGLE, identity.subject(), identity.email(),
                    identity.emailVerified(), true, request.deviceId(), request.deviceInfo(), ipAddress));
        } catch (DataIntegrityViolationException race) {
            return transactionService.authenticateAfterRace(AuthProvider.GOOGLE, identity.subject(),
                            request.deviceId(), request.deviceInfo(), ipAddress)
                    .map(this::map).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT));
        }
    }

    @Override
    public GoogleAuthResponse authenticate(GoogleAuthRequest request, String registrationFlowToken, String ipAddress) {
        VerifiedGoogleIdentity identity = identityVerifier.verify(request.idToken());
        try {
            return map(transactionService.authenticate(AuthProvider.GOOGLE, identity.subject(), identity.email(),
                    identity.emailVerified(), true, registrationFlowToken,
                    request.deviceId(), request.deviceInfo(), ipAddress));
        } catch (DataIntegrityViolationException race) {
            // Request thua unique race đọc lại provider thắng cuộc trong transaction mới, không lộ constraint.
            return transactionService.authenticateAfterRace(AuthProvider.GOOGLE, identity.subject(), request.deviceId(), request.deviceInfo(), ipAddress)
                    .map(this::map)
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT));
        }
    }

    private GoogleAuthResponse map(SocialAuthResult result) {
        return new GoogleAuthResponse(result.accessToken(), result.refreshToken(), GoogleAuthResponse.BEARER_TOKEN_TYPE,
                result.accessTokenExpiresIn(), result.refreshTokenExpiresIn(), result.profileCompleted(), result.nextStep(),
                result.provider(), new GoogleAuthResponse.UserSummary(result.userId(), result.role()));
    }
}
