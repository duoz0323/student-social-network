package com.stu.edu.vn.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LinkPhoneRequest(@NotBlank @Size(max = 32) String phoneNumber) { }
