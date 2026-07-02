package com.stu.edu.vn.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserProfileValidationSupportTest {

    private UserProfileValidationSupport validationSupport;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-07-02T00:00:00Z"), ZoneId.of("UTC"));
        validationSupport = new UserProfileValidationSupport(fixedClock);
    }

    @Test
    void normalizeAndValidateDisplayNameTrimsValidName() {
        String displayName = validationSupport.normalizeAndValidateDisplayName("  Nguyen Van A  ");

        assertThat(displayName).isEqualTo("Nguyen Van A");
    }

    @Test
    void normalizeAndValidateBioConvertsBlankToNull() {
        String bio = validationSupport.normalizeAndValidateBio("   ");

        assertThat(bio).isNull();
    }

    @Test
    void validateDateOfBirthAcceptsUserExactlyEighteenToday() {
        LocalDate dateOfBirth = validationSupport.validateDateOfBirth(LocalDate.of(2008, 7, 2));

        assertThat(dateOfBirth).isEqualTo(LocalDate.of(2008, 7, 2));
    }

    @Test
    void validateDateOfBirthRejectsUserOneDayUnderEighteen() {
        assertThatThrownBy(() -> validationSupport.validateDateOfBirth(LocalDate.of(2008, 7, 3)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_UNDER_MINIMUM_AGE);
    }
}
