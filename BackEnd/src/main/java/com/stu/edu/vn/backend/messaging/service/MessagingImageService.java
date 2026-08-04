package com.stu.edu.vn.backend.messaging.service;

import com.stu.edu.vn.backend.messaging.dto.request.SendImageMessageRequest;
import com.stu.edu.vn.backend.messaging.dto.response.SendMessageResponse;

/** Điều phối gửi ảnh ngoài transaction dài. */
public interface MessagingImageService {
    SendMessageResponse sendImageMessage(Long conversationId, SendImageMessageRequest request);
}
