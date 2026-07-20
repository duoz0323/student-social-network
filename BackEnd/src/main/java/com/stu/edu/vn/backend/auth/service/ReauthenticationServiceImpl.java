package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.ReauthenticationRequest;
import com.stu.edu.vn.backend.auth.dto.ReauthenticationResponse;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.facebook.FacebookAccessTokenVerifier;
import com.stu.edu.vn.backend.auth.google.GoogleIdentityVerifier;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

/** Điều phối proof; Google/Facebook luôn được xác minh ngoài transaction database. */
@Service
public class ReauthenticationServiceImpl implements ReauthenticationService {

    private final CurrentUserProvider currentUserProvider;
    private final GoogleIdentityVerifier googleVerifier;
    private final FacebookAccessTokenVerifier facebookVerifier;
    private final ReauthenticationTransactionService transactionService;

    public ReauthenticationServiceImpl(
            CurrentUserProvider currentUserProvider,
            GoogleIdentityVerifier googleVerifier,
            FacebookAccessTokenVerifier facebookVerifier,
            ReauthenticationTransactionService transactionService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.googleVerifier = googleVerifier;
        this.facebookVerifier = facebookVerifier;
        this.transactionService = transactionService;
    }

    @Override
    public ReauthenticationResponse reauthenticate(ReauthenticationRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        ReauthenticationChallengeCreation creation = switch (request.method()) {
            case PASSWORD -> transactionService.authenticatePassword(
                    userId, request.password(), request.purpose(), request.targetMethod());
            case GOOGLE -> authenticateGoogle(userId, request);
            case FACEBOOK -> authenticateFacebook(userId, request);
        };
        return new ReauthenticationResponse(
                creation.rawToken(), creation.method(), creation.purpose(), creation.targetMethod(),
                creation.expiresAt(), creation.status());
    }

    private ReauthenticationChallengeCreation authenticateGoogle(
            Long userId,
            ReauthenticationRequest request
    ) {
        final String subject;
        try {
            subject = googleVerifier.verify(request.providerCredential()).subject();
        } catch (BusinessException exception) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_CREDENTIAL_INVALID);
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_FAILED);
        }
        return transactionService.authenticateProvider(userId, AuthProvider.GOOGLE, subject,
                request.purpose(), request.targetMethod());
    }

    private ReauthenticationChallengeCreation authenticateFacebook(
            Long userId,
            ReauthenticationRequest request
    ) {
        final String providerUserId;
        try {
            providerUserId = facebookVerifier.verify(request.providerCredential()).providerUserId();
        } catch (BusinessException exception) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_CREDENTIAL_INVALID);
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_FAILED);
        }
        return transactionService.authenticateProvider(userId, AuthProvider.FACEBOOK, providerUserId,
                request.purpose(), request.targetMethod());
    }
}
