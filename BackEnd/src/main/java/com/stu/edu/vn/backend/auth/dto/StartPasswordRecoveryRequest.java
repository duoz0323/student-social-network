package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Identifier được xử lý trung tính để không tiết lộ tài khoản tồn tại. */
public record StartPasswordRecoveryRequest(
        @NotBlank @Size(max = 255) String email,
        @Size(max = 100) String deviceId) { }
