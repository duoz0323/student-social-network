package com.stu.edu.vn.backend.messaging.dto.response;

/** replayed giúp client phân biệt insert 201 với idempotent replay 200. */
public record SendMessageResponse(MessageResponse message, boolean replayed) { }
