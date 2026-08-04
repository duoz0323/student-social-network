package com.stu.edu.vn.backend.messaging.dto.response;

import java.time.LocalDateTime;

/** Tóm tắt message cuối dùng cho Inbox. */
public record LastMessageResponse(Long messageId, Long senderId, String contentPreview, LocalDateTime createdAt) { }
