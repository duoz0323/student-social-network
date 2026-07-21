package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.GoogleAuthProperties;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthResponse;
import com.stu.edu.vn.backend.auth.dto.SocialConflictDetails;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import com.stu.edu.vn.backend.auth.google.SocialChallengeSecurity;
import com.stu.edu.vn.backend.auth.google.VerifiedGoogleIdentity;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
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

/** Transaction database cho ba nhánh Google provider-linked, user mới và account conflict. */
@Service
public class GoogleAuthTransactionService {

    private final UserAuthProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PendingRegistrationRepository pendingRepository;
    private final SocialAuthChallengeRepository challengeRepository;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final GoogleAuthProperties googleProperties;
    private final SocialChallengeSecurity challengeSecurity;
    private final Clock clock;

    public GoogleAuthTransactionService(
            UserAuthProviderRepository providerRepository, UserRepository userRepository,
            UserProfileRepository profileRepository, PendingRegistrationRepository pendingRepository,
            SocialAuthChallengeRepository challengeRepository, RefreshTokenIssuer refreshTokenIssuer,
            JwtService jwtService, JwtProperties jwtProperties, GoogleAuthProperties googleProperties,
            SocialChallengeSecurity challengeSecurity, Clock clock
    ) {
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.pendingRepository = pendingRepository;
        this.challengeRepository = challengeRepository;
        this.refreshTokenIssuer = refreshTokenIssuer;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.googleProperties = googleProperties;
        this.challengeSecurity = challengeSecurity;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = SocialConflictException.class)
    public GoogleAuthResponse authenticate(VerifiedGoogleIdentity identity, String deviceId, String deviceInfo, String ipAddress) {
        NormalizedEmail normalized = normalizedEmail(identity.email());
        Optional<UserAuthProvider> existing = providerRepository.findByProviderAndProviderUserIdForUpdate(
                AuthProvider.GOOGLE, identity.subject());
        if (existing.isPresent()) {
            return issueSession(existing.get().getUser(), deviceId, deviceInfo, ipAddress);
        }

        var pending = pendingRepository.findByActiveIdentifierKeyForUpdate("EMAIL:" + normalized.value());
        if (pending.filter(item -> item.getStatus() == OtpChallengeStatus.PENDING)
                .filter(item -> item.getExpiresAt().isAfter(LocalDateTime.now(clock))).isPresent()) {
            // Hội tụ pending cùng email thuộc Giai đoạn 8; 7A không sửa pending hoặc sao chép password hash.
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_PENDING_CONFLICT);
        }

        Optional<User> emailOwner = userRepository.findByEmail(normalized.value());
        if (emailOwner.isPresent()) {
            throwActiveEmailConflict(identity, normalized.value(), emailOwner.get());
        }

        User user = new User(normalized.value(), null);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        User savedUser = userRepository.saveAndFlush(user);
        profileRepository.saveAndFlush(new UserProfile(savedUser));
        providerRepository.saveAndFlush(new UserAuthProvider(
                savedUser, AuthProvider.GOOGLE, identity.subject(), normalized.value(), true));
        return issueSession(savedUser, deviceId, deviceInfo, ipAddress);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<GoogleAuthResponse> authenticateAfterRace(
            VerifiedGoogleIdentity identity, String deviceId, String deviceInfo, String ipAddress
    ) {
        return providerRepository.findByProviderAndProviderUserIdForUpdate(AuthProvider.GOOGLE, identity.subject())
                .map(link -> issueSession(link.getUser(), deviceId, deviceInfo, ipAddress));
    }

    private void throwActiveEmailConflict(VerifiedGoogleIdentity identity, String email, User conflictingUser) {
        SocialChallengeSecurity.IssuedChallenge issued = challengeSecurity.issue(identity.subject());
        LocalDateTime expiresAt = LocalDateTime.now(clock).plus(googleProperties.getConflictExpiration());
        challengeRepository.saveAndFlush(SocialAuthChallenge.start(
                issued.tokenHash(), AuthProvider.GOOGLE, identity.subject(), issued.identityFingerprint(),
                email, true, SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
                null, conflictingUser, expiresAt));
        SocialConflictDetails details = new SocialConflictDetails(
                issued.rawToken(), SocialConflictDetails.FLOW_TYPE,
                SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
                List.of(SocialResolutionAction.LOGIN_EXISTING_ACCOUNT, SocialResolutionAction.START_ACCOUNT_RECOVERY),
                googleProperties.getConflictExpiration().toSeconds());
        throw new SocialConflictException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT, details);
    }

    private GoogleAuthResponse issueSession(User user, String deviceId, String deviceInfo, String ipAddress) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        UserProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_GOOGLE_AUTHENTICATION_FAILED));
        IssuedRefreshToken refresh = refreshTokenIssuer.issue(user, deviceId, deviceInfo, ipAddress);
        String access = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        boolean completed = profile.getProfileCompletedAt() != null;
        return new GoogleAuthResponse(
                access, refresh.rawToken(), GoogleAuthResponse.BEARER_TOKEN_TYPE,
                Duration.ofMillis(jwtProperties.getAccessTokenExpirationMillis()).toSeconds(),
                refresh.expiresInSeconds(), completed,
                completed ? GoogleAuthResponse.NEXT_STEP_HOME : GoogleAuthResponse.NEXT_STEP_COMPLETE_PROFILE,
                AuthProvider.GOOGLE, new GoogleAuthResponse.UserSummary(user.getId(), user.getRole()));
    }

    private NormalizedEmail normalizedEmail(String email) {
        NormalizedEmail normalized = EmailNormalizer.normalize(email);
        return normalized;
    }
}
