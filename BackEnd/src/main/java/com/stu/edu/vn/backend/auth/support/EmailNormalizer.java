package com.stu.edu.vn.backend.auth.support;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.Locale;
import java.util.regex.Pattern;

/** Chuẩn hóa và kiểm tra email dùng trong mọi luồng xác thực local. */
public final class EmailNormalizer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private EmailNormalizer() { }

    public static NormalizedEmail normalize(String rawEmail) {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.AUTH_IDENTIFIER_INVALID);
        }
        return new NormalizedEmail(email);
    }

    public static boolean isValid(String rawEmail) {
        try {
            normalize(rawEmail);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }
}
