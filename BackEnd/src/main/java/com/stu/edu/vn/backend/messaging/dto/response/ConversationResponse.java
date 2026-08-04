package com.stu.edu.vn.backend.messaging.dto.response;

/** Một item Inbox đã tính unread trực tiếp từ messages. */
public record ConversationResponse(Long conversationId, MessagingUserResponse otherUser,
                                   LastMessageResponse lastMessage, long unreadCount) { }
