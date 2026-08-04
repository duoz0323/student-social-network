package com.stu.edu.vn.backend.messaging.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.messaging.dto.request.SendImageMessageRequest;
import com.stu.edu.vn.backend.messaging.dto.response.SendMessageResponse;
import com.stu.edu.vn.backend.messaging.service.MessagingImageService;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Cùng path gửi text nhưng contract multipart dành cho ảnh; sender luôn lấy từ JWT. */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversations")
public class MessagingImageController {
    private final MessagingImageService service;

    @PostMapping(value = "/{conversationId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendImageMessage(
            @PathVariable @Positive Long conversationId,
            @RequestPart("clientMessageId") String clientMessageId,
            @RequestPart(value = "content", required = false) String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        SendMessageResponse response = service.sendImageMessage(
                conversationId, new SendImageMessageRequest(clientMessageId, content, images));
        HttpStatus status = response.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(ApiResponse.success(
                response.replayed() ? "Phát lại kết quả gửi tin nhắn thành công" : "Gửi tin nhắn thành công", response));
    }
}
