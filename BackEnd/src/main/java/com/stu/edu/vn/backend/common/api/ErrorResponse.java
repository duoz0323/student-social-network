package com.stu.edu.vn.backend.common.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phản hồi lỗi thống nhất, chỉ trả thông tin cần thiết cho Client và không lộ stack trace.
 */
public record ErrorResponse(
        boolean success,
        String code,
        String message,
        int status,
        String path,
        List<FieldErrorDetail> fieldErrors,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(String code, String message, int status, String path) {
        return new ErrorResponse(false, code, message, status, path, List.of(), LocalDateTime.now());
    }

    public static ErrorResponse validation(String message, int status, String path, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(false, "VALIDATION_ERROR", message, status, path, fieldErrors, LocalDateTime.now());
    }
}
