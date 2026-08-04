package com.stu.edu.vn.backend.messaging.dto.response;

import java.time.LocalDateTime;

/** Kết quả marker monotonic và tổng badge đã reconcile sau transaction. */
public record MarkConversationReadResponse(Long conversationId, Long lastReadMessageId,
                                           LocalDateTime lastReadAt, boolean updated,
                                           long totalUnreadCount) { }
