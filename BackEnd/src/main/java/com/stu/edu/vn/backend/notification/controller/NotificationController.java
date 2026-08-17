package com.stu.edu.vn.backend.notification.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.notification.dto.response.DeleteNotificationResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationReadAllResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationReadResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationResponse;
import com.stu.edu.vn.backend.notification.dto.response.NotificationUnreadCountResponse;
import com.stu.edu.vn.backend.notification.dto.response.ModerationNotificationDetailResponse;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API hộp thông báo chỉ cho phép current user đọc và quản lý dữ liệu của chính mình.
 */
@Validated
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        validatePagination(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách thông báo thành công",
                notificationService.getNotifications(page, size)
        ));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationUnreadCountResponse>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy số thông báo chưa đọc thành công",
                notificationService.getUnreadCount()
        ));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> markAsRead(
            @PathVariable @Positive Long notificationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đánh dấu thông báo đã đọc thành công",
                notificationService.markAsRead(notificationId)
        ));
    }

    @GetMapping("/{notificationId}/moderation-detail")
    public ResponseEntity<ApiResponse<ModerationNotificationDetailResponse>> getModerationDetail(
            @PathVariable @Positive Long notificationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết quyết định kiểm duyệt thành công",
                notificationService.getModerationDetail(notificationId)
        ));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<NotificationReadAllResponse>> markAllAsRead() {
        return ResponseEntity.ok(ApiResponse.success(
                "Đánh dấu tất cả thông báo đã đọc thành công",
                notificationService.markAllAsRead()
        ));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<DeleteNotificationResponse>> deleteNotification(
            @PathVariable @Positive Long notificationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa thông báo thành công",
                notificationService.deleteNotification(notificationId)
        ));
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
