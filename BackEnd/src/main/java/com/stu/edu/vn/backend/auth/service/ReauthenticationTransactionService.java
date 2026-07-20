package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.ReauthenticationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationProofMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transaction ngắn phát hành proof đã xác minh; không thực hiện external provider call. */
@Service
public class ReauthenticationTransactionService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository providerRepository;
    private final ReauthenticationChallengeRepository challengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FlowTokenGenerator flowTokenGenerator;
    private final AuthHmacService hmacService;
    private final ReauthenticationProperties properties;
    private final Clock clock;

    public ReauthenticationTransactionService(
            UserRepository userRepository,
            UserAuthProviderRepository providerRepository,
            ReauthenticationChallengeRepository challengeRepository,
            PasswordEncoder passwordEncoder,
            FlowTokenGenerator flowTokenGenerator,
            AuthHmacService hmacService,
            ReauthenticationProperties properties,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.challengeRepository = challengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.flowTokenGenerator = flowTokenGenerator;
        this.hmacService = hmacService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public ReauthenticationChallengeCreation authenticatePassword(
            Long userId,
            String password,
            ReauthenticationScope purpose,
            AuthMethod targetMethod
    ) {
        User user = activeUser(userId);
        if (user.getPasswordHash() == null) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_METHOD_UNAVAILABLE);
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_CREDENTIAL_INVALID);
        }
        return replaceChallenge(user, ReauthenticationMethod.PASSWORD,
                ReauthenticationProofMethod.LOCAL_PASSWORD, purpose, targetMethod);
    }

    @Transactional
    public ReauthenticationChallengeCreation authenticateProvider(
            Long userId,
            AuthProvider provider,
            String providerUserId,
            ReauthenticationScope purpose,
            AuthMethod targetMethod
    ) {
        User user = activeUser(userId);
        UserAuthProvider link = providerRepository
                .findByProviderAndProviderUserIdForUpdate(provider, providerUserId)
                .filter(existing -> existing.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_PROVIDER_NOT_LINKED));
        ReauthenticationMethod method = link.getProvider() == AuthProvider.GOOGLE
                ? ReauthenticationMethod.GOOGLE : ReauthenticationMethod.FACEBOOK;
        ReauthenticationProofMethod proofMethod = link.getProvider() == AuthProvider.GOOGLE
                ? ReauthenticationProofMethod.GOOGLE : ReauthenticationProofMethod.FACEBOOK;
        return replaceChallenge(user, method, proofMethod, purpose, targetMethod);
    }

    private User activeUser(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        return user;
    }

    private ReauthenticationChallengeCreation replaceChallenge(
            User user,
            ReauthenticationMethod method,
            ReauthenticationProofMethod proofMethod,
            ReauthenticationScope purpose,
            AuthMethod targetMethod
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        String activeKey = user.getId() + ":" + purpose.name();

        // Khóa challenge cũ cùng user/scope để hai lần reauthenticate không tạo hai token ACTIVE.
        challengeRepository.findByActiveUserScopeKeyForUpdate(activeKey).ifPresent(existing -> {
            if (existing.getExpiresAt().isAfter(now)) {
                existing.cancel(now);
            } else {
                existing.expire(now);
            }
            challengeRepository.saveAndFlush(existing);
        });

        String rawToken = flowTokenGenerator.generate();
        LocalDateTime expiresAt = now.plus(properties.getExpiration());
        ReauthenticationChallenge challenge;
        try {
            challenge = ReauthenticationChallenge.start(
                    user,
                    hmacService.hashFlowToken(rawToken),
                    proofMethod,
                    purpose,
                    targetMethod,
                    expiresAt
            );
            challengeRepository.saveAndFlush(challenge);
        } catch (BusinessException exception) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_FAILED);
        }
        return new ReauthenticationChallengeCreation(
                rawToken,
                method,
                purpose,
                targetMethod,
                expiresAt,
                ReauthenticationChallengeStatus.ACTIVE
        );
    }
}
