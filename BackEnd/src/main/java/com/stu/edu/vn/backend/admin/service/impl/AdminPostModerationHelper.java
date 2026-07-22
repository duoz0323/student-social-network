package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.user.entity.User;
import java.time.LocalDateTime;

/** Dùng chung phép chuyển Post PUBLISHED sang HIDDEN giữa Admin Post và Admin Report. */
final class AdminPostModerationHelper {

    private AdminPostModerationHelper() {
        // Không khởi tạo helper thuần domain operation.
    }

    static void hidePublishedPost(
            Post post,
            User admin,
            LocalDateTime hiddenAt,
            AdminPostHideReason reason
    ) {
        // Caller phải kiểm tra state machine trước; helper chỉ thực hiện một transition hợp lệ duy nhất.
        post.setStatus(PostStatus.HIDDEN);
        post.setHiddenBy(admin);
        post.setHiddenAt(hiddenAt);
        post.setHiddenReason(reason.name());
    }
}
