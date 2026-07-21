package com.stu.edu.vn.backend.auth.support;

import org.springframework.stereotype.Component;

/** Che email trước khi trả Client để không lộ toàn bộ địa chỉ. */
@Component
public class EmailMasker {
    public String mask(NormalizedEmail normalizedEmail) {
        String email = normalizedEmail.value();
        int separator = email.indexOf('@');
        String localPart = email.substring(0, separator);
        return localPart.substring(0, 1) + "***" + email.substring(separator);
    }
}
