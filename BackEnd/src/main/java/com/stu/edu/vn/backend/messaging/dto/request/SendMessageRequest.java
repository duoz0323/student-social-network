package com.stu.edu.vn.backend.messaging.dto.request;

/** Sender và type không xuất hiện trong request vì Backend tự suy ra từ JWT và sharedPostId. */
public record SendMessageRequest(String clientMessageId, String content, Long sharedPostId) {
    public SendMessageRequest(String clientMessageId, String content) {
        this(clientMessageId, content, null);
    }
}
