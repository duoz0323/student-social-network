package com.stu.edu.vn.backend.messaging.dto.response;

import com.stu.edu.vn.backend.messaging.enums.MessageAttachmentMediaType;

/** Metadata an toàn của ảnh chat, cố ý không chứa URL hay storage public ID. */
public record MessageAttachmentResponse(Long attachmentId, MessageAttachmentMediaType mediaType,
        String mimeType, Long fileSizeBytes, Integer width, Integer height, Byte displayOrder) { }
