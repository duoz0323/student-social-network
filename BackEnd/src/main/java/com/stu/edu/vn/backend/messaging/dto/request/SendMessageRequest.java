package com.stu.edu.vn.backend.messaging.dto.request;

/** Sender và type không xuất hiện trong request vì Backend quyết định từ JWT và contract TEXT. */
public record SendMessageRequest(String clientMessageId, String content) { }
