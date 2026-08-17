package com.stu.edu.vn.backend.post.dto.response;

import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import java.util.List;

/**
 * Thông tin công khai tối thiểu của tác giả bài viết, không trả email hoặc số điện thoại.
 */
public record PostAuthorResponse(
        Long id,
        String displayName,
        String avatarUrl,
        List<PublicUserBadge> badges
) {
    public PostAuthorResponse(Long id, String displayName, String avatarUrl) {
        this(id, displayName, avatarUrl, List.of());
    }
}
