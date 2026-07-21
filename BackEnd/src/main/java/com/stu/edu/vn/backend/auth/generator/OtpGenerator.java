package com.stu.edu.vn.backend.auth.generator;

/** Sinh OTP dạng số và cho phép thay thế bằng mock trong kiểm thử. */
public interface OtpGenerator {

    String generate();
}
