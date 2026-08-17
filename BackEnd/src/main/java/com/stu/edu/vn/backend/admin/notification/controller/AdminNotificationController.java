package com.stu.edu.vn.backend.admin.notification.controller;

import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationMutationResponse;
import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationResponse;
import com.stu.edu.vn.backend.admin.notification.dto.AdminNotificationUnreadCountResponse;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API không nhận adminId; recipient luôn được lấy từ JWT Principal. */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notifications")
public class AdminNotificationController {
    private final AdminNotificationService service;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<AdminNotificationResponse>>> list(
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông báo quản trị thành công",
                service.getNotifications(limit, cursor)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<AdminNotificationUnreadCountResponse>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success("Lấy số thông báo quản trị chưa đọc thành công",
                service.getUnreadCount()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<AdminNotificationMutationResponse>> read(
            @PathVariable @Positive Long notificationId) {
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu thông báo quản trị", service.markRead(notificationId)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> readAll() {
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu các thông báo quản trị đang hiển thị",
                service.markAllRead()));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<AdminNotificationMutationResponse>> delete(
            @PathVariable @Positive Long notificationId) {
        return ResponseEntity.ok(ApiResponse.success("Đã xóa thông báo quản trị", service.delete(notificationId)));
    }
}
