package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import com.stu.edu.vn.backend.auth.crypto.AuthHmacService;
import com.stu.edu.vn.backend.auth.entity.PendingRegistration;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import com.stu.edu.vn.backend.auth.generator.FlowTokenGenerator;
import com.stu.edu.vn.backend.auth.generator.OtpGenerator;
import com.stu.edu.vn.backend.auth.repository.PendingRegistrationRepository;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transaction ngắn chỉ kiểm tra và ghi pending; tuyệt đối không gọi email trong lớp này. */
@Service
@RequiredArgsConstructor
public class RegistrationTransactionService {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;
    private final FlowTokenGenerator flowTokenGenerator;
    private final AuthHmacService authHmacService;
    private final AuthRegistrationProperties properties;
    private final Clock clock;

    @Transactional
    public RegistrationCreation create(NormalizedEmail identifier, String rawPassword) {
        ensureIdentifierDoesNotBelongToUser(identifier);

        RegistrationType registrationType = RegistrationType.EMAIL;
        String activeKey = registrationType.name() + ":" + identifier.value();
        LocalDateTime now = LocalDateTime.now(clock);

        var existingPending = pendingRegistrationRepository.findByActiveIdentifierKeyForUpdate(activeKey);
        if (existingPending.isPresent()) {
            PendingRegistration existing = existingPending.get();
            if (existing.getExpiresAt().isAfter(now)) {
                String rawFlowToken = flowTokenGenerator.generate();
                existing.resumeWithFlowToken(authHmacService.hashFlowToken(rawFlowToken));
                pendingRegistrationRepository.saveAndFlush(existing);
                return new RegistrationCreation(
                        registrationType, identifier.value(), null, rawFlowToken,
                        existing.getFlowTokenHash(), existing.getOtpExpiresAt(),
                        existing.getResendAvailableAt(), existing.getExpiresAt(), true
                );
            }
            // Kết thúc bản ghi cũ và flush active key về NULL trước khi chèn bản ghi thay thế.
            existing.expire(now);
            pendingRegistrationRepository.saveAndFlush(existing);
        }

        String rawOtp = otpGenerator.generate();
        String rawFlowToken = flowTokenGenerator.generate();
        LocalDateTime otpExpiresAt = now.plus(properties.getOtpExpiration());
        LocalDateTime resendAvailableAt = now.plus(properties.getResendCooldown());
        LocalDateTime pendingExpiresAt = now.plus(properties.getPendingExpiration());

        PendingRegistration pending = PendingRegistration.start(
                registrationType,
                identifier.value(),
                passwordEncoder.encode(rawPassword),
                authHmacService.hashFlowToken(rawFlowToken),
                authHmacService.hashOtp(rawOtp),
                otpExpiresAt,
                resendAvailableAt,
                pendingExpiresAt
        );
        pendingRegistrationRepository.saveAndFlush(pending);

        return new RegistrationCreation(
                registrationType,
                identifier.value(),
                rawOtp,
                rawFlowToken,
                pending.getFlowTokenHash(),
                otpExpiresAt,
                resendAvailableAt,
                pendingExpiresAt,
                false
        );
    }

    private void ensureIdentifierDoesNotBelongToUser(NormalizedEmail identifier) {
        if (userRepository.existsByEmail(identifier.value())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }

}
