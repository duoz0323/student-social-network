package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Gom validation hồ sơ dùng chung cho onboarding và cập nhật hồ sơ sau onboarding.
 */
@Component
@RequiredArgsConstructor
public class UserProfileValidationSupport {

    private static final int MIN_DISPLAY_NAME_LENGTH = 2;
    private static final int MAX_DISPLAY_NAME_LENGTH = 100;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final int MAX_BIO_LENGTH = 500;
    private static final int MINIMUM_AGE = 18;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._]+$");
    private static final Set<String> RESERVED_USERNAMES = Set.of(
            "admin", "administrator", "system", "api", "auth", "login", "register",
            "signup", "settings", "support", "help", "moderator"
    );

    private final Clock clock;

    public String normalizeAndValidateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.USERNAME_REQUIRED);
        }
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (normalizedUsername.length() < MIN_USERNAME_LENGTH
                || normalizedUsername.length() > MAX_USERNAME_LENGTH
                || !USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            throw new BusinessException(ErrorCode.USERNAME_INVALID);
        }
        if (RESERVED_USERNAMES.contains(normalizedUsername)) {
            throw new BusinessException(ErrorCode.USERNAME_RESERVED);
        }
        return normalizedUsername;
    }

    public String normalizeAndValidateDisplayName(String displayName) {
        String normalizedDisplayName = normalizeRequiredText(displayName);
        if (normalizedDisplayName == null
                || normalizedDisplayName.length() < MIN_DISPLAY_NAME_LENGTH
                || normalizedDisplayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_DISPLAY_NAME);
        }
        return normalizedDisplayName;
    }

    public LocalDate validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new BusinessException(ErrorCode.INVALID_DATE_OF_BIRTH);
        }
        LocalDate today = LocalDate.now(clock);
        if (dateOfBirth.isAfter(today)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_OF_BIRTH, "Ngày sinh không được nằm trong tương lai");
        }
        if (dateOfBirth.plusYears(MINIMUM_AGE).isAfter(today)) {
            throw new BusinessException(ErrorCode.USER_UNDER_MINIMUM_AGE);
        }
        return dateOfBirth;
    }

    public String normalizeAndValidateBio(String bio) {
        String normalizedBio = normalizeOptionalText(bio);
        if (normalizedBio != null && normalizedBio.length() > MAX_BIO_LENGTH) {
            throw new BusinessException(ErrorCode.BIO_TOO_LONG);
        }
        return normalizedBio;
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }
}
