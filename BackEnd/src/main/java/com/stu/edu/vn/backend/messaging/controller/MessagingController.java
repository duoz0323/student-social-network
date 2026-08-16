package com.stu.edu.vn.backend.messaging.controller;

import com.stu.edu.vn.backend.common.api.*;
import com.stu.edu.vn.backend.messaging.dto.request.*;
import com.stu.edu.vn.backend.messaging.dto.response.*;
import com.stu.edu.vn.backend.messaging.service.MessagingService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** REST Core Messaging lấy actor từ SecurityContext và không tiếp nhận mutation qua STOMP. */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversations")
public class MessagingController {
    private final MessagingService messagingService;

    @GetMapping
    public ApiResponse<CursorPageResponse<ConversationResponse>> getConversations(
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
            @RequestParam(required = false) String cursor) {
        return ApiResponse.success("Lấy danh sách cuộc trò chuyện thành công",
                messagingService.getConversations(limit, cursor));
    }

    @GetMapping("/unread-count")
    public ApiResponse<MessagingUnreadCountResponse> getUnreadCount() {
        return ApiResponse.success("Lấy số tin nhắn chưa đọc thành công", messagingService.getUnreadCount());
    }

    @GetMapping("/share-recipients")
    public ApiResponse<PageResponse<ShareRecipientResponse>> getShareRecipients(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return ApiResponse.success("Lấy danh sách người nhận bài viết thành công",
                messagingService.getShareRecipients(keyword, page, size));
    }

    @PutMapping("/direct/{recipientUserId}")
    public ApiResponse<DirectConversationResponse> openDirectConversation(
            @PathVariable @Positive Long recipientUserId) {
        return ApiResponse.success("Mở cuộc trò chuyện thành công",
                messagingService.openDirectConversation(recipientUserId));
    }

    @GetMapping("/{conversationId}/messages")
    public ApiResponse<CursorPageResponse<MessageResponse>> getMessages(
            @PathVariable @Positive Long conversationId,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) String cursor) {
        return ApiResponse.success("Lấy lịch sử tin nhắn thành công",
                messagingService.getMessages(conversationId, limit, cursor));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
            @PathVariable @Positive Long conversationId,
            @RequestBody SendMessageRequest request) {
        SendMessageResponse response = messagingService.sendMessage(conversationId, request);
        HttpStatus status = response.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(ApiResponse.success(
                response.replayed() ? "Phát lại kết quả gửi tin nhắn thành công" : "Gửi tin nhắn thành công", response));
    }

    @PutMapping("/{conversationId}/read")
    public ApiResponse<MarkConversationReadResponse> markRead(
            @PathVariable @Positive Long conversationId,
            @RequestBody MarkConversationReadRequest request) {
        return ApiResponse.success("Cập nhật trạng thái đã đọc thành công",
                messagingService.markRead(conversationId, request));
    }
}
