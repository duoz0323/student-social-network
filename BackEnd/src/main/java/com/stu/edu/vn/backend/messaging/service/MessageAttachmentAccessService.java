package com.stu.edu.vn.backend.messaging.service;

import com.stu.edu.vn.backend.messaging.dto.response.MessageAttachmentAccessResponse;

/** Cấp quyền truy cập ngắn hạn sau khi kiểm tra lại toàn bộ visibility. */
public interface MessageAttachmentAccessService {
    MessageAttachmentAccessResponse createAccess(Long attachmentId);
}
