package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthResponse;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.facebook.FacebookAccessTokenVerifier;
import com.stu.edu.vn.backend.auth.facebook.VerifiedFacebookIdentity;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Điều phối Facebook Auth; Graph API luôn chạy trước transaction database. */
@Service
public class FacebookAuthServiceImpl implements FacebookAuthService {
    private final FacebookAccessTokenVerifier verifier;
    private final SocialAuthenticationTransactionService transactionService;

    public FacebookAuthServiceImpl(FacebookAccessTokenVerifier verifier, SocialAuthenticationTransactionService transactionService) {
        this.verifier = verifier; this.transactionService = transactionService;
    }

    @Override
    public FacebookAuthResponse authenticate(FacebookAuthRequest request, String ipAddress) {
        VerifiedFacebookIdentity identity = verifier.verify(request.accessToken());
        try {
            return map(transactionService.authenticate(AuthProvider.FACEBOOK, identity.providerUserId(), identity.email(),
                    identity.email() == null ? null : true, true,
                    request.deviceId(), request.deviceInfo(), ipAddress));
        } catch (DataIntegrityViolationException race) {
            return transactionService.authenticateAfterRace(AuthProvider.FACEBOOK, identity.providerUserId(),
                            request.deviceId(), request.deviceInfo(), ipAddress)
                    .map(this::map).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT));
        }
    }

    @Override
    public FacebookAuthResponse authenticate(FacebookAuthRequest request, String registrationFlowToken, String ipAddress) {
        VerifiedFacebookIdentity identity = verifier.verify(request.accessToken());
        try {
            return map(transactionService.authenticate(AuthProvider.FACEBOOK, identity.providerUserId(), identity.email(),
                    identity.email() == null ? null : true, true, registrationFlowToken,
                    request.deviceId(), request.deviceInfo(), ipAddress));
        } catch (DataIntegrityViolationException race) {
            // Unique constraint là tuyến phòng thủ cuối; request thua chỉ được đọc provider thắng cuộc.
            return transactionService.authenticateAfterRace(AuthProvider.FACEBOOK, identity.providerUserId(),
                            request.deviceId(), request.deviceInfo(), ipAddress)
                    .map(this::map).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT));
        }
    }

    private FacebookAuthResponse map(SocialAuthResult result) {
        return new FacebookAuthResponse(result.accessToken(), result.refreshToken(), "Bearer",
                result.accessTokenExpiresIn(), result.refreshTokenExpiresIn(), result.profileCompleted(),
                result.nextStep(), result.provider(), new FacebookAuthResponse.UserSummary(result.userId(), result.role()));
    }
}
