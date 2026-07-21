package com.stu.edu.vn.backend.common.api;

import java.time.LocalDateTime;

/**
 * Phản hồi thành công thống nhất để Controller không trả Entity trực tiếp ra API.
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
}
