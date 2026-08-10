package com.stu.edu.vn.backend.user.dto.response;

/** Kết quả kiểm tra username phục vụ phản hồi tức thời trên màn hình onboarding. */
public record UsernameAvailabilityResponse(
        String username,
        boolean available
) {
}
