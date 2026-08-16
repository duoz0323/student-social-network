package com.stu.edu.vn.backend.messaging.dto.response;

/** Recipient đã được Backend lọc đúng quyền bắt đầu hoặc tiếp tục conversation. */
public record ShareRecipientResponse(Long userId, String username, String displayName,
                                     String avatarUrl, Long conversationId,
                                     boolean existingConversation) { }
