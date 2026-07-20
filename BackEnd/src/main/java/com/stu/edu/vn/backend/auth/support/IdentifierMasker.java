package com.stu.edu.vn.backend.auth.support;

import org.springframework.stereotype.Component;

/** Che identifier trước khi trả Client để không lộ toàn bộ email hoặc số điện thoại. */
@Component
public class IdentifierMasker {

    public String mask(NormalizedIdentifier identifier) {
        if (identifier.type() == IdentifierType.EMAIL) {
            return maskEmail(identifier.value());
        }
        return maskPhone(identifier.value());
    }

    String maskEmail(String email) {
        int separator = email.indexOf('@');
        String localPart = email.substring(0, separator);
        String visiblePrefix = localPart.substring(0, 1);
        return visiblePrefix + "***" + email.substring(separator);
    }

    String maskPhone(String phoneNumber) {
        int visibleDigits = Math.min(4, phoneNumber.length());
        return "*".repeat(phoneNumber.length() - visibleDigits)
                + phoneNumber.substring(phoneNumber.length() - visibleDigits);
    }
}
