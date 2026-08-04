package com.stu.edu.vn.backend.messaging.dto.request;

/** Client chỉ được yêu cầu marker cụ thể đã quan sát, Backend không tự suy diễn tin cuối. */
public record MarkConversationReadRequest(Long lastReadMessageId) { }
