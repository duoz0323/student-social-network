package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.entity.ReauthenticationChallenge;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationChallengeStatus;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import com.stu.edu.vn.backend.auth.repository.ReauthenticationChallengeRepository;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transaction nguyên tử gỡ một auth method của chính user hiện tại. */
@Service
public class AuthMethodUnlinkTransactionService {
    private final UserRepository userRepository;
    private final UserAuthProviderRepository providerRepository;
    private final ReauthenticationChallengeRepository challengeRepository;
    private final AuthHmacService hmacService;
    private final Clock clock;

    public AuthMethodUnlinkTransactionService(UserRepository userRepository,
            UserAuthProviderRepository providerRepository,
            ReauthenticationChallengeRepository challengeRepository,
            AuthHmacService hmacService, Clock clock) {
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.challengeRepository = challengeRepository;
        this.hmacService = hmacService;
        this.clock = clock;
    }

    @Transactional
    public void unlink(Long userId, AuthMethod method, String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_REQUIRED);
        }
        LocalDateTime now = LocalDateTime.now(clock);
        // Khóa user để các link/unlink đồng thời luôn đếm lại phương thức trên cùng trạng thái mới nhất.
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        ReauthenticationChallenge challenge = challengeRepository
                .findByTokenHashForUpdate(hmacService.hashFlowToken(rawToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_INVALID));
        validateChallenge(challenge, userId, method, now);

        List<UserAuthProvider> social = providerRepository.findAllByUserIdOrderByProviderAsc(userId);
        ensureLinked(user, social, method);
        if (completeLocalCount(user) + social.size() <= 1) {
            throw new BusinessException(ErrorCode.AUTH_LAST_LOGIN_METHOD_CANNOT_BE_REMOVED);
        }

        switch (method) {
            case EMAIL -> {
                user.setEmail(null);
                user.setEmailVerifiedAt(null);
            }
            case GOOGLE, FACEBOOK -> deleteSocial(userId, method);
        }
        // Không giữ password hash không còn gắn với local identifier đăng nhập hợp lệ.
        if (!hasCompleteLocalMethod(user)) user.setPasswordHash(null);
        challenge.consume(now);
        challengeRepository.saveAndFlush(challenge);
        userRepository.saveAndFlush(user);
    }

    private void validateChallenge(ReauthenticationChallenge challenge, Long userId,
            AuthMethod method, LocalDateTime now) {
        if (!challenge.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_INVALID);
        }
        if (challenge.getScope() != ReauthenticationScope.UNLINK_AUTH_METHOD
                || challenge.getTargetAuthMethod() != method) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_PURPOSE_INVALID);
        }
        if (challenge.getStatus() != ReauthenticationChallengeStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_ALREADY_USED);
        }
        if (!challenge.getExpiresAt().isAfter(now)) {
            throw new BusinessException(ErrorCode.AUTH_REAUTHENTICATION_EXPIRED);
        }
    }

    private void ensureLinked(User user, List<UserAuthProvider> providers, AuthMethod method) {
        boolean linked = switch (method) {
            case EMAIL -> user.getEmail() != null && user.getEmailVerifiedAt() != null;
            case GOOGLE -> hasProvider(providers, AuthProvider.GOOGLE);
            case FACEBOOK -> hasProvider(providers, AuthProvider.FACEBOOK);
        };
        if (!linked) throw new BusinessException(ErrorCode.AUTH_METHOD_NOT_LINKED);
    }

    private void deleteSocial(Long userId, AuthMethod method) {
        AuthProvider provider = method == AuthMethod.GOOGLE ? AuthProvider.GOOGLE : AuthProvider.FACEBOOK;
        UserAuthProvider link = providerRepository.findByUserIdAndProviderForUpdate(userId, provider)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_METHOD_NOT_LINKED));
        providerRepository.delete(link);
        providerRepository.flush();
    }

    private int completeLocalCount(User user) {
        if (user.getPasswordHash() == null) return 0;
        int count = 0;
        if (user.getEmail() != null && user.getEmailVerifiedAt() != null) count++;
        return count;
    }

    private boolean hasCompleteLocalMethod(User user) {
        return user.getPasswordHash() != null && user.getEmail() != null && user.getEmailVerifiedAt() != null;
    }

    private boolean hasProvider(List<UserAuthProvider> providers, AuthProvider provider) {
        return providers.stream().anyMatch(link -> link.getProvider() == provider);
    }
}
