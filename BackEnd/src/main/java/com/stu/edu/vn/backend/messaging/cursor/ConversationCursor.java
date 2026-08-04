package com.stu.edu.vn.backend.messaging.cursor;

import java.time.LocalDateTime;

/** Cursor Inbox chứa đúng hai khóa sắp xếp giảm dần. */
public record ConversationCursor(LocalDateTime lastMessageAt, Long conversationId) { }
