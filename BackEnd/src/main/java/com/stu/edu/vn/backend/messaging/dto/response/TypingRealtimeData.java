package com.stu.edu.vn.backend.messaging.dto.response;

/** Payload typing tối thiểu, không chứa profile hoặc dữ liệu xác thực. */
public record TypingRealtimeData(Long conversationId, Long userId) { }
