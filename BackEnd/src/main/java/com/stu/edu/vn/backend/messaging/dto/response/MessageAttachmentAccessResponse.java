package com.stu.edu.vn.backend.messaging.dto.response;

import java.time.OffsetDateTime;

/** URL ký ngắn hạn chỉ được cấp sau khi kiểm tra lại quyền truy cập. */
public record MessageAttachmentAccessResponse(Long attachmentId, String accessUrl, OffsetDateTime expiresAt) { }
