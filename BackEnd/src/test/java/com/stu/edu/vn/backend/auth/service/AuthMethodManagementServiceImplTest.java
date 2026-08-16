package com.stu.edu.vn.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.auth.delivery.RegistrationOtpSender;
import com.stu.edu.vn.backend.auth.dto.AuthMethodResponse;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.facebook.FacebookAccessTokenVerifier;
import com.stu.edu.vn.backend.auth.google.GoogleIdentityVerifier;
import com.stu.edu.vn.backend.auth.google.VerifiedGoogleIdentity;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.auth.support.EmailMasker;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthMethodManagementServiceImplTest {
    @Mock CurrentUserProvider currentUserProvider;
    @Mock UserRepository userRepository;
    @Mock UserAuthProviderRepository providerRepository;
    @Mock AuthMethodLinkTransactionService linkTransactionService;
    @Mock SocialProviderLinkTransactionService providerLinkTransactionService;
    @Mock AuthMethodUnlinkTransactionService unlinkTransactionService;
    @Mock RegistrationOtpSender otpSender;
    @Mock GoogleIdentityVerifier googleVerifier;
    @Mock FacebookAccessTokenVerifier facebookVerifier;

    private AuthMethodManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthMethodManagementServiceImpl(currentUserProvider, userRepository, providerRepository,
                linkTransactionService, providerLinkTransactionService, unlinkTransactionService,
                otpSender, new EmailMasker(),
                googleVerifier, facebookVerifier);
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
    }

    @Test
    void socialOnlyVerifiedEmailIsVisibleButLocalLoginIsUnavailable() {
        User user = new User("student@example.com", null);
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setEmailVerifiedAt(LocalDateTime.now());
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(providerRepository.findAllByUserIdOrderByProviderAsc(7L)).thenReturn(List.of());

        var result = service.list();

        assertThat(result.methods()).hasSize(3);
        assertThat(result.methods().get(0)).satisfies(method -> {
            assertThat(method.type()).isEqualTo(AuthMethod.EMAIL);
            assertThat(method.maskedIdentifier()).isEqualTo("s***@example.com");
            assertThat(method.localLoginAvailable()).isFalse();
            assertThat(method.state()).isEqualTo(com.stu.edu.vn.backend.auth.enums.EmailLoginState.VERIFIED_NO_PASSWORD);
            assertThat(method.canSetPassword()).isTrue();
        });
    }

    @Test
    void googleCredentialIsVerifiedBeforeTransactionServiceReceivesIdentity() {
        GoogleAuthRequest request = new GoogleAuthRequest("id-token", null, null);
        when(googleVerifier.verify("id-token")).thenReturn(new VerifiedGoogleIdentity(
                "google-sub", "other@example.com", true, null, null, "issuer"));
        AuthMethodResponse linked = new AuthMethodResponse(AuthMethod.GOOGLE, null, true, null, true, false);
        when(providerLinkTransactionService.link(7L, AuthProvider.GOOGLE, "google-sub",
                "other@example.com", true)).thenReturn(linked);

        assertThat(service.linkGoogle(request)).isSameAs(linked);
        verify(googleVerifier).verify("id-token");
        verify(providerLinkTransactionService).link(7L, AuthProvider.GOOGLE, "google-sub",
                "other@example.com", true);
    }
}
