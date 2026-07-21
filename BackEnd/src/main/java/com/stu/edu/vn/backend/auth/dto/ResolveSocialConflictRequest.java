package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.SocialResolutionAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request giải quyết social conflict theo action đã được contract cho phép. */
public record ResolveSocialConflictRequest(
        @NotNull(message = "Hành động giải quyết không được để trống")
        SocialResolutionAction action,
        @Size(max = 255, message = "deviceId không được vượt quá 255 ký tự")
        String deviceId,
        @Size(max = 500, message = "deviceInfo không được vượt quá 500 ký tự")
        String deviceInfo
) { }
