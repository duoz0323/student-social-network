package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LinkOtpRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{6}$") String code
) { }
