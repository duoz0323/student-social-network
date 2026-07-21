package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictRequest;
import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictResponse;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.entity.SocialAuthChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.OtpChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialAuthChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.SocialConflictType;
import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.repository.SocialAuthChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.JwtProperties;
import com.stu.edu.vn.backend.security.JwtService;
import com.stu.edu.vn.backend.security.TokenHashService;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolve challenge chỉ dùng snapshot identity đã được verifier xác minh ở social Auth. */
@Service
public class SocialConflictResolutionServiceImpl implements SocialConflictResolutionService {
    private final SocialAuthChallengeRepository challengeRepository;
    private final PendingRegistrationRepository pendingRepository;
    private final UserAuthProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHashService tokenHashService;
    private final Clock clock;

    public SocialConflictResolutionServiceImpl(SocialAuthChallengeRepository challengeRepository,
            PendingRegistrationRepository pendingRepository, UserAuthProviderRepository providerRepository,
            UserRepository userRepository, UserProfileRepository profileRepository,
            RefreshTokenIssuer refreshTokenIssuer, JwtService jwtService, JwtProperties jwtProperties,
            TokenHashService tokenHashService, Clock clock) {
        this.challengeRepository = challengeRepository;
        this.pendingRepository = pendingRepository;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenIssuer = refreshTokenIssuer;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenHashService = tokenHashService;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public ResolveSocialConflictResponse resolve(String rawChallengeToken, ResolveSocialConflictRequest request,
            String ipAddress) {
        if (rawChallengeToken == null || rawChallengeToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_CHALLENGE_INVALID);
        }
        SocialAuthChallenge challenge = challengeRepository
                .findByConflictTokenHashForUpdate(tokenHashService.sha256Hex(rawChallengeToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_SOCIAL_CHALLENGE_INVALID));
        LocalDateTime now = LocalDateTime.now(clock);
        validateChallenge(challenge, now);
        if (challenge.getConflictType() == SocialConflictType.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER) {
            // Contract hiện tại chỉ hướng dẫn login/recovery, không tự link provider tại endpoint này.
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_CHALLENGE_ACTION_INVALID);
        }

        PendingRegistration pending = pendingRepository.findByIdForUpdate(challenge.getPendingRegistration().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REGISTRATION_FLOW_INVALID));
        validatePending(pending, now);
        if (request.action() == SocialResolutionAction.CONTINUE_OTP) {
            challenge.resolve(SocialResolutionAction.CONTINUE_OTP, null, now);
            challengeRepository.saveAndFlush(challenge);
            return ResolveSocialConflictResponse.continueOtp();
        }
        if (request.action() != SocialResolutionAction.CANCEL_PENDING_AND_CONTINUE_SOCIAL) {
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_CHALLENGE_ACTION_INVALID);
        }

        if (providerRepository.findByProviderAndProviderUserIdForUpdate(
                challenge.getProvider(), challenge.getProviderUserId()).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_PROVIDER_ALREADY_LINKED);
        }
        if (challenge.getProviderEmail() != null && userRepository.findByEmail(challenge.getProviderEmail()).isPresent()) {
            // Không hủy pending nếu email social trong lúc chờ đã thuộc một tài khoản khác.
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_ACCOUNT_CONFLICT);
        }

        User user = new User(challenge.getProviderEmail(), null);
        if (challenge.getProviderEmail() != null && Boolean.TRUE.equals(challenge.getProviderEmailVerified())) {
            user.setEmailVerifiedAt(now);
        }
        User savedUser = userRepository.saveAndFlush(user);
        profileRepository.saveAndFlush(new UserProfile(savedUser));
        providerRepository.saveAndFlush(new UserAuthProvider(savedUser, challenge.getProvider(),
                challenge.getProviderUserId(), challenge.getProviderEmail(), challenge.getProviderEmailVerified()));
        IssuedRefreshToken refresh = refreshTokenIssuer.issue(
                savedUser, request.deviceId(), request.deviceInfo(), ipAddress);

        // Chỉ hủy pending sau khi mọi kiểm tra identity/provider đã thành công trong cùng transaction.
        pending.cancel(now);
        pendingRepository.saveAndFlush(pending);
        challenge.resolve(SocialResolutionAction.CANCEL_PENDING_AND_CONTINUE_SOCIAL, savedUser, now);
        challengeRepository.saveAndFlush(challenge);
        SocialAuthResult result = new SocialAuthResult(
                jwtService.generateAccessToken(savedUser.getId(), savedUser.getRole().name()), refresh.rawToken(),
                Duration.ofMillis(jwtProperties.getAccessTokenExpirationMillis()).toSeconds(),
                refresh.expiresInSeconds(), false, "COMPLETE_PROFILE", challenge.getProvider(),
                savedUser.getId(), savedUser.getRole());
        return ResolveSocialConflictResponse.session(result);
    }

    private void validateChallenge(SocialAuthChallenge challenge, LocalDateTime now) {
        if (challenge.getStatus() == SocialAuthChallengeStatus.RESOLVED) {
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_CHALLENGE_ALREADY_USED);
        }
        if (challenge.getStatus() == SocialAuthChallengeStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_CHALLENGE_CANCELLED);
        }
        if (challenge.getStatus() == SocialAuthChallengeStatus.EXPIRED || !challenge.getExpiresAt().isAfter(now)) {
            if (challenge.getStatus() == SocialAuthChallengeStatus.PENDING) {
                challenge.expire(now);
                challengeRepository.saveAndFlush(challenge);
            }
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_CHALLENGE_EXPIRED);
        }
    }

    private void validatePending(PendingRegistration pending, LocalDateTime now) {
        if (pending.getStatus() != OtpChallengeStatus.PENDING) {
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_NOT_PENDING);
        }
        if (!pending.getExpiresAt().isAfter(now)) {
            pending.expire(now);
            pendingRepository.saveAndFlush(pending);
            throw new BusinessException(ErrorCode.AUTH_REGISTRATION_EXPIRED);
        }
    }
}
