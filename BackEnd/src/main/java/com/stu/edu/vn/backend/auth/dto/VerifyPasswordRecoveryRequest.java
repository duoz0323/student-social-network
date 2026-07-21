package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPasswordRecoveryRequest(
        @NotBlank @Pattern(regexp = "\\d{6}") String code) { }
