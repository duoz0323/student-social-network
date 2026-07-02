package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Gom validation hồ sơ dùng chung cho onboarding và cập nhật hồ sơ sau onboarding.
 */
@Component
class UserProfileValidationSupport {

    private static final int MIN_DISPLAY_NAME_LENGTH = 2;
    private static final int MAX_DISPLAY_NAME_LENGTH = 100;
    private static final int MAX_BIO_LENGTH = 500;
    private static final int MINIMUM_AGE = 18;

    private final Clock clock;

    UserProfileValidationSupport(Clock clock) {
        this.clock = clock;
    }

    String normalizeAndValidateDisplayName(String displayName) {
        String normalizedDisplayName = normalizeRequiredText(displayName);
        if (normalizedDisplayName == null
                || normalizedDisplayName.length() < MIN_DISPLAY_NAME_LENGTH
                || normalizedDisplayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_DISPLAY_NAME);
        }
        return normalizedDisplayName;
    }

    LocalDate validateDateOfBirth(LocalDate dateOfBirth) {
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

    String normalizeAndValidateBio(String bio) {
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
