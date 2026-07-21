package com.stu.edu.vn.backend.auth.enums;

/** State machine của challenge khôi phục mật khẩu. */
public enum PasswordRecoveryStatus { PENDING, VERIFIED, COMPLETED, EXPIRED, LOCKED }
