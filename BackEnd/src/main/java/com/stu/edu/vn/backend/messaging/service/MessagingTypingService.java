package com.stu.edu.vn.backend.messaging.service;

import com.stu.edu.vn.backend.messaging.dto.request.TypingRequest;

/** Xử lý frame typing tạm thời mà không đọc hoặc ghi Message. */
public interface MessagingTypingService {
    void handleTyping(String principalName, TypingRequest request);
}
