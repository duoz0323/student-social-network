package com.stu.edu.vn.backend.auth.support;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Chuẩn hóa và nhận diện identifier theo phạm vi MVP: email hoặc số điện thoại nội bộ 10 chữ số.
 */
public final class IdentifierNormalizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");

    private IdentifierNormalizer() {
    }

    public static NormalizedIdentifier normalize(String rawIdentifier) {
        String email = normalizeEmail(rawIdentifier);
        if (EMAIL_PATTERN.matcher(email).matches()) {
            return new NormalizedIdentifier(IdentifierType.EMAIL, email);
        }

        String phoneNumber = normalizePhoneNumber(rawIdentifier);
        if (PHONE_PATTERN.matcher(phoneNumber).matches()) {
            return new NormalizedIdentifier(IdentifierType.PHONE_NUMBER, phoneNumber);
        }

        throw new BusinessException(ErrorCode.INVALID_IDENTIFIER);
    }

    public static boolean isSupported(String rawIdentifier) {
        try {
            normalize(rawIdentifier);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    public static String normalizeEmail(String rawIdentifier) {
        if (rawIdentifier == null) {
            return "";
        }
        return rawIdentifier.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    public static String normalizePhoneNumber(String rawIdentifier) {
        if (rawIdentifier == null) {
            return "";
        }
        return rawIdentifier.replaceAll("[\\s.-]+", "");
    }
}
