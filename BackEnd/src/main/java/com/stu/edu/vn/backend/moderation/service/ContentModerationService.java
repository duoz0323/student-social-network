package com.stu.edu.vn.backend.moderation.service;

import com.stu.edu.vn.backend.moderation.dto.ModerationResult;

/** Pipeline moderation text dùng chung; Backend mới là nơi thực thi ALLOW/WARNING/BLOCK. */
public interface ContentModerationService {
    ModerationResult moderate(String content);

    void requireAllowed(String content);
}
