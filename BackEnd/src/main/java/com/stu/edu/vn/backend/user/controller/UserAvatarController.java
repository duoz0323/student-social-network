package com.stu.edu.vn.backend.user.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.user.dto.response.AvatarResponse;
import com.stu.edu.vn.backend.user.service.UserAvatarService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller avatar chỉ nhận file upload, không nhận userId hoặc public_id từ Client.
 */
@RestController
@RequestMapping("/api/v1/users/me/avatar")
public class UserAvatarController {

    private final UserAvatarService userAvatarService;

    public UserAvatarController(UserAvatarService userAvatarService) {
        this.userAvatarService = userAvatarService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AvatarResponse>> uploadMyAvatar(@RequestParam("file") MultipartFile file) {
        AvatarResponse response = userAvatarService.uploadMyAvatar(file);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<AvatarResponse>> deleteMyAvatar() {
        AvatarResponse response = userAvatarService.deleteMyAvatar();
        return ResponseEntity.ok(ApiResponse.success("Ảnh đại diện đã được xóa", response));
    }
}
