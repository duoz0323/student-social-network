package com.stu.edu.vn.backend.messaging.dto.request;

/** Client chỉ khai báo conversation và trạng thái; danh tính luôn lấy từ Principal. */
public record TypingRequest(Long conversationId, Boolean typing) { }
