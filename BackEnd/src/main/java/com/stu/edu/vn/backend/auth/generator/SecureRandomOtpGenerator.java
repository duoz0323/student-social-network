package com.stu.edu.vn.backend.auth.generator;

import com.stu.edu.vn.backend.auth.config.AuthRegistrationProperties;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Sinh OTP bằng nguồn ngẫu nhiên mật mã, không ghi OTP vào log. */
@Component
public class SecureRandomOtpGenerator implements OtpGenerator {

    private final SecureRandom secureRandom;
    private final int otpLength;

    @Autowired
    public SecureRandomOtpGenerator(AuthRegistrationProperties properties) {
        this(new SecureRandom(), properties.getOtpLength());
    }

    SecureRandomOtpGenerator(SecureRandom secureRandom, int otpLength) {
        if (otpLength < 1 || otpLength > 9) {
            throw new IllegalArgumentException("OTP length phải nằm trong khoảng 1 đến 9");
        }
        this.secureRandom = secureRandom;
        this.otpLength = otpLength;
    }

    @Override
    public String generate() {
        int upperBound = (int) Math.pow(10, otpLength);
        return String.format("%0" + otpLength + "d", secureRandom.nextInt(upperBound));
    }
}
