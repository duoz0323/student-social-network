package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompletePasswordRecoveryRequest(
        @NotBlank @Size(max = 72) String newPassword,
        @NotBlank @Size(max = 72) String confirmPassword) { }
