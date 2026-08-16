package com.stu.edu.vn.backend.messaging.dto.response;

import java.time.LocalDateTime;
import com.stu.edu.vn.backend.messaging.enums.MessageType;

/** Tóm tắt message cuối dùng cho Inbox. */
public record LastMessageResponse(Long messageId, Long senderId, MessageType type,
                                  String contentPreview, LocalDateTime createdAt) { }
