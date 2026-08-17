package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.GoogleAuthProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.dto.SocialConflictDetails;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import com.stu.edu.vn.backend.auth.google.SocialChallengeSecurity;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.auth.support.AccountBlockedErrors;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Nghiệp vụ dùng chung cho Google/Facebook sau khi danh tính provider đã được xác minh. */
@Service
public class SocialAuthenticationTransactionService {
    private final UserAuthProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PendingRegistrationRepository pendingRepository;
    private final SocialAuthChallengeRepository challengeRepository;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final GoogleAuthProperties challengeProperties;
    private final SocialChallengeSecurity challengeSecurity;
    private final AuthHmacService authHmacService;
    private final Clock clock;

    public SocialAuthenticationTransactionService(UserAuthProviderRepository providerRepository,
            UserRepository userRepository, UserProfileRepository profileRepository,
            PendingRegistrationRepository pendingRepository, SocialAuthChallengeRepository challengeRepository,
            RefreshTokenIssuer refreshTokenIssuer, JwtService jwtService, JwtProperties jwtProperties,
            GoogleAuthProperties challengeProperties, SocialChallengeSecurity challengeSecurity,
            AuthHmacService authHmacService, Clock clock) {
        this.providerRepository = providerRepository; this.userRepository = userRepository;
        this.profileRepository = profileRepository; this.pendingRepository = pendingRepository;
        this.challengeRepository = challengeRepository; this.refreshTokenIssuer = refreshTokenIssuer;
        this.jwtService = jwtService; this.jwtProperties = jwtProperties;
        this.challengeProperties = challengeProperties; this.challengeSecurity = challengeSecurity;
        this.authHmacService = authHmacService; this.clock = clock;
    }

    @Transactional(noRollbackFor = SocialConflictException.class)
    public SocialAuthResult authenticate(AuthProvider provider, String providerUserId, String providerEmail,
            Boolean providerEmailVerified, boolean allowMissingEmail,
            String deviceId, String deviceInfo, String ipAddress) {
        return authenticate(provider, providerUserId, providerEmail, providerEmailVerified, allowMissingEmail,
                null, deviceId, deviceInfo, ipAddress);
    }

    @Transactional(noRollbackFor = SocialConflictException.class)
    public SocialAuthResult authenticate(AuthProvider provider, String providerUserId, String providerEmail,
            Boolean providerEmailVerified, boolean allowMissingEmail, String registrationFlowToken,
            String deviceId, String deviceInfo, String ipAddress) {
        Optional<UserAuthProvider> existing = providerRepository.findByProviderAndProviderUserIdForUpdate(provider, providerUserId);
        if (existing.isPresent()) return issueSession(existing.get().getUser(), provider, deviceId, deviceInfo, ipAddress);

        String email = normalizeOptionalEmail(providerEmail);
        String verifiedEmail = Boolean.TRUE.equals(providerEmailVerified) ? email : null;
        if (registrationFlowToken != null && !registrationFlowToken.isBlank()) {
            return authenticateWithPending(provider, providerUserId, email, providerEmailVerified,
                    registrationFlowToken, deviceId, deviceInfo, ipAddress);
        }
        if (verifiedEmail != null) {
            var pending = pendingRepository.findByActiveIdentifierKeyForUpdate("EMAIL:" + verifiedEmail);
            if (pending.filter(item -> item.getStatus() == OtpChallengeStatus.PENDING)
                    .filter(item -> item.getExpiresAt().isAfter(LocalDateTime.now(clock))).isPresent()) {
                // Hội tụ pending/social thuộc Giai đoạn 8; không sửa pending trong luồng này.
                throw new BusinessException(ErrorCode.AUTH_SOCIAL_PENDING_CONFLICT);
            }
            Optional<User> emailOwner = userRepository.findByEmail(verifiedEmail);
            if (emailOwner.isPresent()) throwActiveEmailConflict(provider, providerUserId,
                    verifiedEmail, providerEmailVerified, emailOwner.get());
        } else if (!allowMissingEmail) {
            throw new BusinessException(provider == AuthProvider.FACEBOOK
                    ? ErrorCode.AUTH_FACEBOOK_EMAIL_MISSING : ErrorCode.AUTH_GOOGLE_EMAIL_MISSING);
        }

        // Chỉ verified Google email được đưa vào users; Facebook email chỉ là metadata của provider.
        String accountEmail = provider == AuthProvider.GOOGLE ? verifiedEmail : null;
        User savedUser = userRepository.saveAndFlush(new User(accountEmail, null));
        if (accountEmail != null) savedUser.setEmailVerifiedAt(LocalDateTime.now(clock));
        profileRepository.saveAndFlush(new UserProfile(savedUser));
        providerRepository.saveAndFlush(new UserAuthProvider(savedUser, provider, providerUserId, email,
                email == null ? null : providerEmailVerified));
        return issueSession(savedUser, provider, deviceId, deviceInfo, ipAddress);
    }

    private SocialAuthResult authenticateWithPending(AuthProvider provider, String providerUserId, String email,
            Boolean providerEmailVerified, String rawFlowToken, String deviceId, String deviceInfo, String ipAddress) {
        var pending = pendingRepository.findByFlowTokenHashForUpdate(authHmacService.hashFlowToken(rawFlowToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID));
        LocalDateTime now = LocalDateTime.now(clock);
        if (pending.getStatus() != OtpChallengeStatus.PENDING) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_NOT_PENDING);
        }
        if (!pending.getExpiresAt().isAfter(now)) {
            pending.expire(now);
            pendingRepository.saveAndFlush(pending);
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_EXPIRED);
        }

        boolean sameVerifiedEmail = pending.getRegistrationType() == RegistrationType.EMAIL
                && email != null
                && Boolean.TRUE.equals(providerEmailVerified)
                && pending.getIdentifierNormalized().equals(email);
        if (sameVerifiedEmail) {
            // Social identity đã xác minh cùng email được phép hoàn tất pending và giữ password local.
            Optional<User> emailOwner = userRepository.findByEmail(email);
            if (emailOwner.isPresent()) {
                throwActiveEmailConflict(provider, providerUserId, email, providerEmailVerified, emailOwner.get());
            }
            User user = new User(email, pending.getPasswordHash());
            user.setEmailVerifiedAt(now);
            User savedUser = userRepository.saveAndFlush(user);
            profileRepository.saveAndFlush(new UserProfile(savedUser));
            providerRepository.saveAndFlush(new UserAuthProvider(
                    savedUser, provider, providerUserId, email, providerEmailVerified));
            IssuedRefreshToken refresh = refreshTokenIssuer.issue(savedUser, deviceId, deviceInfo, ipAddress);
            pending.complete(savedUser, now);
            pendingRepository.saveAndFlush(pending);
            return sessionResult(savedUser, provider, refresh, false);
        }

        SocialConflictType conflictType = pending.getRegistrationType() == RegistrationType.EMAIL
                ? SocialConflictType.PENDING_EMAIL_MISMATCH
                : SocialConflictType.PENDING_EMAIL_MISMATCH;
        throwPendingConflict(provider, providerUserId, email, providerEmailVerified, pending, conflictType);
        throw new IllegalStateException("Unreachable");
    }

    private void throwPendingConflict(AuthProvider provider, String providerUserId, String email,
            Boolean verified, com.stu.edu.vn.backend.auth.entity.PendingRegistration pending,
            SocialConflictType conflictType) {
        SocialChallengeSecurity.IssuedChallenge issued = challengeSecurity.issue(providerUserId);
        challengeRepository.findByActiveProviderKeyForUpdate(provider.name() + ":" + issued.identityFingerprint())
                .ifPresent(existing -> existing.cancel(LocalDateTime.now(clock)));
        challengeRepository.saveAndFlush(SocialAuthChallenge.start(issued.tokenHash(), provider, providerUserId,
                issued.identityFingerprint(), email, email == null ? null : verified, conflictType,
                pending, null, LocalDateTime.now(clock).plus(challengeProperties.getConflictExpiration())));
        SocialConflictDetails details = new SocialConflictDetails(issued.rawToken(), SocialConflictDetails.FLOW_TYPE,
                conflictType, List.of(SocialResolutionAction.CONTINUE_OTP,
                SocialResolutionAction.CANCEL_PENDING_AND_CONTINUE_SOCIAL),
                challengeProperties.getConflictExpiration().toSeconds());
        throw new SocialConflictException(ErrorCode.AUTH_SOCIAL_PENDING_CONFLICT, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<SocialAuthResult> authenticateAfterRace(AuthProvider provider, String providerUserId,
            String deviceId, String deviceInfo, String ipAddress) {
        return providerRepository.findByProviderAndProviderUserIdForUpdate(provider, providerUserId)
                .map(link -> issueSession(link.getUser(), provider, deviceId, deviceInfo, ipAddress));
    }

    private void throwActiveEmailConflict(AuthProvider provider, String providerUserId, String email,
            Boolean verified, User conflictingUser) {
        SocialChallengeSecurity.IssuedChallenge issued = challengeSecurity.issue(providerUserId);
        challengeRepository.saveAndFlush(SocialAuthChallenge.start(issued.tokenHash(), provider, providerUserId,
                issued.identityFingerprint(), email, verified, SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
                null, conflictingUser, LocalDateTime.now(clock).plus(challengeProperties.getConflictExpiration())));
        SocialConflictDetails details = new SocialConflictDetails(issued.rawToken(), SocialConflictDetails.FLOW_TYPE,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
                List.of(SocialResolutionAction.LOGIN_EXISTING_ACCOUNT, SocialResolutionAction.START_ACCOUNT_RECOVERY),
                challengeProperties.getConflictExpiration().toSeconds());
        throw new SocialConflictException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT, details);
    }

    private SocialAuthResult issueSession(User user, AuthProvider provider, String deviceId, String deviceInfo, String ipAddress) {
        if (user.getStatus() != UserStatus.ACTIVE) throw AccountBlockedErrors.forUser(user);
        UserProfile profile = profileRepository.findById(user.getId()).orElseThrow(() -> new BusinessException(
                provider == AuthProvider.FACEBOOK ? ErrorCode.AUTH_FACEBOOK_AUTHENTICATION_FAILED : ErrorCode.AUTH_GOOGLE_AUTHENTICATION_FAILED));
        IssuedRefreshToken refresh = refreshTokenIssuer.issue(user, deviceId, deviceInfo, ipAddress);
        boolean completed = profile.isCompleted();
        return sessionResult(user, provider, refresh, completed);
    }

    private SocialAuthResult sessionResult(User user, AuthProvider provider, IssuedRefreshToken refresh, boolean completed) {
        return new SocialAuthResult(jwtService.generateAccessToken(user.getId(), user.getRole().name()), refresh.rawToken(),
                Duration.ofMillis(jwtProperties.getAccessTokenExpirationMillis()).toSeconds(), refresh.expiresInSeconds(),
                completed, completed ? "HOME" : "COMPLETE_PROFILE", provider, user.getId(), user.getRole());
    }

    private String normalizeOptionalEmail(String email) {
        if (email == null || email.isBlank()) return null;
        var normalized = EmailNormalizer.normalize(email);
        return normalized.value();
    }
}
