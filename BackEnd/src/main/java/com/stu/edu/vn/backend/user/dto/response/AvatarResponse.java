package com.stu.edu.vn.backend.user.dto.response;

/**
 * Response avatar chỉ trả URL cần hiển thị, không trả public_id nội bộ của Cloudinary cho Client.
 */
public record AvatarResponse(
        String avatarUrl,
        boolean profileCompleted
) {
}
