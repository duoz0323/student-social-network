package com.stu.edu.vn.backend.messaging.dto.response;

/** Kết quả open/create không tạo message và cho biết conversation đã có lịch sử hay chưa. */
public record DirectConversationResponse(Long conversationId, MessagingUserResponse otherUser, boolean hasMessages) { }
