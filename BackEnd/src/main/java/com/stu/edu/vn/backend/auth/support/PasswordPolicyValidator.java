package com.stu.edu.vn.backend.auth.support;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Kiểm tra policy mật khẩu local trước khi thực hiện BCrypt. */
@Component
public class PasswordPolicyValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$"
    );

    public boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
