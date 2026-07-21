package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryOutcome;
import com.stu.edu.vn.backend.auth.delivery.OtpDeliveryResult;
import com.stu.edu.vn.backend.auth.delivery.RegistrationOtpSender;
import com.stu.edu.vn.backend.auth.dto.AuthMethodResponse;
import com.stu.edu.vn.backend.auth.dto.AuthMethodsResponse;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.dto.LinkChallengeResponse;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.facebook.FacebookAccessTokenVerifier;
import com.stu.edu.vn.backend.auth.google.GoogleIdentityVerifier;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.auth.support.EmailMasker;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Điều phối API quản lý auth method; mọi external verifier/sender chạy ngoài transaction. */
@Service
public class AuthMethodManagementServiceImpl implements AuthMethodManagementService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserAuthProviderRepository providerRepository;
    private final AuthMethodLinkTransactionService linkTransactionService;
    private final SocialProviderLinkTransactionService providerLinkTransactionService;
    private final AuthMethodUnlinkTransactionService unlinkTransactionService;
    private final RegistrationOtpSender otpSender;
    private final EmailMasker masker;
    private final GoogleIdentityVerifier googleVerifier;
    private final FacebookAccessTokenVerifier facebookVerifier;

    public AuthMethodManagementServiceImpl(CurrentUserProvider currentUserProvider, UserRepository userRepository,
            UserAuthProviderRepository providerRepository, AuthMethodLinkTransactionService linkTransactionService,
            SocialProviderLinkTransactionService providerLinkTransactionService,
            AuthMethodUnlinkTransactionService unlinkTransactionService, RegistrationOtpSender otpSender,
            EmailMasker masker, GoogleIdentityVerifier googleVerifier,
            FacebookAccessTokenVerifier facebookVerifier) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.linkTransactionService = linkTransactionService;
        this.providerLinkTransactionService = providerLinkTransactionService;
        this.unlinkTransactionService = unlinkTransactionService;
        this.otpSender = otpSender;
        this.masker = masker;
        this.googleVerifier = googleVerifier;
        this.facebookVerifier = facebookVerifier;
    }

    @Override
    public AuthMethodsResponse list() {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.USER_BLOCKED);
        List<UserAuthProvider> providers = providerRepository.findAllByUserIdOrderByProviderAsc(userId);
        int loginMethodCount = completeLocalCount(user) + providers.size();
        List<AuthMethodResponse> methods = new ArrayList<>();
        if (user.getEmail() != null && user.getEmailVerifiedAt() != null) {
            methods.add(local(user, AuthMethod.EMAIL, user.getEmail(), loginMethodCount));
        }
        providers.forEach(link -> methods.add(new AuthMethodResponse(
                link.getProvider() == AuthProvider.GOOGLE ? AuthMethod.GOOGLE : AuthMethod.FACEBOOK,
                null, true, link.getCreatedAt(), loginMethodCount > 1, false)));
        return new AuthMethodsResponse(List.copyOf(methods));
    }

    @Override
    public LinkChallengeResponse start(NormalizedEmail identifier, AuthMethodLinkPurpose purpose) {
        try {
            return deliver(linkTransactionService.start(currentUserProvider.getCurrentUserId(), identifier, purpose));
        } catch (DataIntegrityViolationException race) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_IN_USE);
        }
    }

    @Override
    public LinkChallengeResponse resend(String token, AuthMethodLinkPurpose purpose) {
        return deliver(linkTransactionService.resend(currentUserProvider.getCurrentUserId(), token, purpose));
    }

    @Override
    public AuthMethodResponse verify(String token, String code, AuthMethodLinkPurpose purpose) {
        return linkTransactionService.verify(currentUserProvider.getCurrentUserId(), token, code, purpose);
    }

    @Override
    public AuthMethodResponse linkGoogle(GoogleAuthRequest request) {
        var identity = googleVerifier.verify(request.idToken());
        return linkProvider(AuthProvider.GOOGLE, identity.subject(), identity.email(), identity.emailVerified());
    }

    @Override
    public AuthMethodResponse linkFacebook(FacebookAuthRequest request) {
        var identity = facebookVerifier.verify(request.accessToken());
        return linkProvider(AuthProvider.FACEBOOK, identity.providerUserId(), identity.email(),
                identity.email() == null ? null : true);
    }

    @Override
    public void unlink(AuthMethod method, String reauthenticationToken) {
        unlinkTransactionService.unlink(currentUserProvider.getCurrentUserId(), method, reauthenticationToken);
    }

    private AuthMethodResponse linkProvider(AuthProvider provider, String subject, String email, Boolean verified) {
        try {
            return providerLinkTransactionService.link(currentUserProvider.getCurrentUserId(), provider,
                    subject, email, verified);
        } catch (DataIntegrityViolationException race) {
            throw new BusinessException(ErrorCode.AUTH_PROVIDER_LINK_CONFLICT);
        }
    }

    private LinkChallengeResponse deliver(LinkChallengeCreation creation) {
        OtpDeliveryResult result;
        try {
            result = otpSender.send(creation.deliveryType(), creation.identifier(), creation.rawOtp());
        } catch (RuntimeException exception) {
            result = OtpDeliveryResult.unknown();
        }
        linkTransactionService.recordDelivery(creation.challengeId(), creation.otpVersion(), result);
        if (result.outcome() != OtpDeliveryOutcome.SENT)
            throw new BusinessException(ErrorCode.AUTH_OTP_DELIVERY_FAILED);
        return new LinkChallengeResponse(creation.rawFlowToken(), creation.purpose(),
                masker.mask(new NormalizedEmail(creation.identifier())),
                creation.otpExpiresAt(), creation.resendAvailableAt(), creation.expiresAt());
    }

    private int completeLocalCount(User user) {
        if (user.getPasswordHash() == null) return 0;
        int count = 0;
        if (user.getEmail() != null && user.getEmailVerifiedAt() != null) count++;
        return count;
    }

    private AuthMethodResponse local(User user, AuthMethod method, String value, int count) {
        boolean available = user.getPasswordHash() != null;
        return new AuthMethodResponse(method, masker.mask(new NormalizedEmail(value)), true,
                user.getCreatedAt(), available && count > 1, available);
    }
}
