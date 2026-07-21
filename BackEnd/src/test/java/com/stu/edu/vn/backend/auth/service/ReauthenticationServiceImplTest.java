package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.dto.ReauthenticationRequest;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.facebook.FacebookAccessTokenVerifier;
import com.stu.edu.vn.backend.auth.facebook.VerifiedFacebookIdentity;
import com.stu.edu.vn.backend.auth.google.GoogleIdentityVerifier;
import com.stu.edu.vn.backend.auth.google.VerifiedGoogleIdentity;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReauthenticationServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final GoogleIdentityVerifier googleVerifier = org.mockito.Mockito.mock(GoogleIdentityVerifier.class);
    private final FacebookAccessTokenVerifier facebookVerifier =
            org.mockito.Mockito.mock(FacebookAccessTokenVerifier.class);
    private final ReauthenticationTransactionService transactionService =
            org.mockito.Mockito.mock(ReauthenticationTransactionService.class);
    private ReauthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReauthenticationServiceImpl(
                currentUserProvider, googleVerifier, facebookVerifier, transactionService);
        when(currentUserProvider.getCurrentUserId()).thenReturn(21L);
    }

    @Test
    void googleVerifierIdentityNotEmailIsPassedToTransaction() {
        ReauthenticationRequest request = new ReauthenticationRequest(
                ReauthenticationMethod.GOOGLE, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.EMAIL, null, "google-token");
        when(googleVerifier.verify("google-token")).thenReturn(new VerifiedGoogleIdentity(
                "google-sub", "different@example.com", true, null, null, "accounts.google.com"));
        when(transactionService.authenticateProvider(21L, AuthProvider.GOOGLE, "google-sub",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.EMAIL))
                .thenReturn(creation(ReauthenticationMethod.GOOGLE, AuthMethod.EMAIL));

        var response = service.reauthenticate(request);

        assertThat(response.reauthenticationToken()).isEqualTo("raw-token");
        verify(transactionService).authenticateProvider(21L, AuthProvider.GOOGLE, "google-sub",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.EMAIL);
    }

    @Test
    void facebookReauthenticationDoesNotRequireEmail() {
        ReauthenticationRequest request = new ReauthenticationRequest(
                ReauthenticationMethod.FACEBOOK, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.GOOGLE, null, "facebook-token");
        when(facebookVerifier.verify("facebook-token")).thenReturn(new VerifiedFacebookIdentity(
                "facebook-id", null, null, null, "app-id", Instant.now().plusSeconds(300)));
        when(transactionService.authenticateProvider(21L, AuthProvider.FACEBOOK, "facebook-id",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.GOOGLE))
                .thenReturn(creation(ReauthenticationMethod.FACEBOOK, AuthMethod.GOOGLE));

        service.reauthenticate(request);

        verify(transactionService).authenticateProvider(21L, AuthProvider.FACEBOOK, "facebook-id",
                ReauthenticationScope.UNLINK_AUTH_METHOD, AuthMethod.GOOGLE);
    }

    @Test
    void invalidProviderCredentialIsMappedToSafeError() {
        ReauthenticationRequest request = new ReauthenticationRequest(
                ReauthenticationMethod.GOOGLE, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.FACEBOOK, null, "invalid-token");
        when(googleVerifier.verify("invalid-token"))
                .thenThrow(new BusinessException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID));

        assertThatThrownBy(() -> service.reauthenticate(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REAUTHENTICATION_CREDENTIAL_INVALID);
    }

    @Test
    void invalidFacebookCredentialIsMappedToSafeError() {
        ReauthenticationRequest request = new ReauthenticationRequest(
                ReauthenticationMethod.FACEBOOK, ReauthenticationScope.UNLINK_AUTH_METHOD,
                AuthMethod.GOOGLE, null, "invalid-facebook-token");
        when(facebookVerifier.verify("invalid-facebook-token"))
                .thenThrow(new BusinessException(ErrorCode.AUTH_FACEBOOK_TOKEN_INVALID));

        assertThatThrownBy(() -> service.reauthenticate(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_REAUTHENTICATION_CREDENTIAL_INVALID);
    }

    private ReauthenticationChallengeCreation creation(ReauthenticationMethod method, AuthMethod target) {
        return new ReauthenticationChallengeCreation(
                "raw-token", method, ReauthenticationScope.UNLINK_AUTH_METHOD, target,
                LocalDateTime.of(2026, 7, 20, 10, 5), ReauthenticationChallengeStatus.ACTIVE);
    }
}
