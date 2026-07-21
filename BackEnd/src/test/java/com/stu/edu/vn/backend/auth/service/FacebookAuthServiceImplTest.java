package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.facebook.FacebookAccessTokenVerifier;
import com.stu.edu.vn.backend.auth.facebook.VerifiedFacebookIdentity;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.enums.UserRole;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class FacebookAuthServiceImplTest {
    private final FacebookAccessTokenVerifier verifier = org.mockito.Mockito.mock(FacebookAccessTokenVerifier.class);
    private final SocialAuthenticationTransactionService transactionService =
            org.mockito.Mockito.mock(SocialAuthenticationTransactionService.class);
    private FacebookAuthServiceImpl service;

    @BeforeEach
    void setUp() { service = new FacebookAuthServiceImpl(verifier, transactionService); }

    @Test
    void verifiedProviderReturnsOnlySystemSession() {
        FacebookAuthRequest request = new FacebookAuthRequest("facebook-token", "device", "browser");
        when(verifier.verify("facebook-token")).thenReturn(identity("facebook@example.com"));
        when(transactionService.authenticate(AuthProvider.FACEBOOK, "fb-123", "facebook@example.com", true,
                true, "device", "browser", "127.0.0.1"))
                .thenReturn(result());

        var response = service.authenticate(request, "127.0.0.1");

        assertThat(response.accessToken()).isEqualTo("system-access");
        assertThat(response.refreshToken()).isEqualTo("system-refresh");
        assertThat(response.authenticationMethod()).isEqualTo(AuthProvider.FACEBOOK);
    }

    @Test
    void missingEmailIsPassedAsProviderOnlyIdentityWithoutPlaceholder() {
        FacebookAuthRequest request = new FacebookAuthRequest("facebook-token", null, null);
        when(verifier.verify("facebook-token")).thenReturn(identity(null));
        when(transactionService.authenticate(AuthProvider.FACEBOOK, "fb-123", null, null,
                true, null, null, "127.0.0.1")).thenReturn(result());

        service.authenticate(request, "127.0.0.1");

        verify(transactionService).authenticate(AuthProvider.FACEBOOK, "fb-123", null, null,
                true, null, null, "127.0.0.1");
    }

    @Test
    void uniqueRaceWithoutWinningProviderBecomesSafeConflict() {
        FacebookAuthRequest request = new FacebookAuthRequest("facebook-token", null, null);
        when(verifier.verify("facebook-token")).thenReturn(identity(null));
        when(transactionService.authenticate(AuthProvider.FACEBOOK, "fb-123", null, null,
                true, null, null, "127.0.0.1")).thenThrow(new DataIntegrityViolationException("constraint"));
        when(transactionService.authenticateAfterRace(AuthProvider.FACEBOOK, "fb-123", null, null, "127.0.0.1"))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.authenticate(request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT);
    }

    private VerifiedFacebookIdentity identity(String email) {
        return new VerifiedFacebookIdentity("fb-123", email, "Student", null, "app", Instant.now().plusSeconds(60));
    }
    private SocialAuthResult result() {
        return new SocialAuthResult("system-access", "system-refresh", 900, 3600,
                false, "COMPLETE_PROFILE", AuthProvider.FACEBOOK, 1L, UserRole.USER);
    }
}
