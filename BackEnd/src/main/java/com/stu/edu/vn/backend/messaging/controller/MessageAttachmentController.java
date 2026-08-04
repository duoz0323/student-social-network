package com.stu.edu.vn.backend.messaging.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.messaging.dto.response.MessageAttachmentAccessResponse;
import com.stu.edu.vn.backend.messaging.service.MessageAttachmentAccessService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Endpoint chống IDOR: biết attachmentId không đồng nghĩa có quyền xem file. */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message-attachments")
public class MessageAttachmentController {
    private final MessageAttachmentAccessService service;

    @GetMapping("/{attachmentId}/access")
    public ApiResponse<MessageAttachmentAccessResponse> createAccess(@PathVariable @Positive Long attachmentId) {
        return ApiResponse.success("Cấp quyền truy cập ảnh tin nhắn thành công", service.createAccess(attachmentId));
    }
}
