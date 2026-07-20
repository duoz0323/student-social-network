package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.AuthMethodResponse;
import com.stu.edu.vn.backend.auth.entity.UserAuthProvider;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthProvider;
import com.stu.edu.vn.backend.auth.repository.UserAuthProviderRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Link provider đã xác minh vào đúng user từ JWT; không dùng social email để chọn user đích. */
@Service
public class SocialProviderLinkTransactionService {
    private final UserRepository userRepository;
    private final UserAuthProviderRepository providerRepository;

    public SocialProviderLinkTransactionService(UserRepository userRepository,
            UserAuthProviderRepository providerRepository) {
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
    }

    @Transactional
    public AuthMethodResponse link(Long userId, AuthProvider provider, String providerUserId,
            String providerEmail, Boolean providerEmailVerified) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.USER_BLOCKED);

        var identityOwner = providerRepository.findByProviderAndProviderUserIdForUpdate(provider, providerUserId);
        if (identityOwner.isPresent()) {
            if (!identityOwner.get().getUser().getId().equals(userId))
                throw new BusinessException(ErrorCode.AUTH_PROVIDER_ALREADY_LINKED);
            return response(identityOwner.get());
        }
        var currentProvider = providerRepository.findByUserIdAndProvider(userId, provider);
        if (currentProvider.isPresent())
            throw new BusinessException(ErrorCode.AUTH_PROVIDER_LINK_CONFLICT);

        UserAuthProvider saved = providerRepository.saveAndFlush(new UserAuthProvider(
                user, provider, providerUserId, providerEmail, providerEmailVerified));
        return response(saved);
    }

    private AuthMethodResponse response(UserAuthProvider link) {
        AuthMethod method = link.getProvider() == AuthProvider.GOOGLE ? AuthMethod.GOOGLE : AuthMethod.FACEBOOK;
        return new AuthMethodResponse(method, null, true, link.getCreatedAt(), true, false);
    }
}
