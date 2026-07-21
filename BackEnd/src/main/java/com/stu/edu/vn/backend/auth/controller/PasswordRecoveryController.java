package com.stu.edu.vn.backend.auth.controller;

import com.stu.edu.vn.backend.auth.dto.CompletePasswordRecoveryRequest;
import com.stu.edu.vn.backend.auth.dto.CompletePasswordRecoveryResponse;
import com.stu.edu.vn.backend.auth.dto.PasswordRecoveryChallengeResponse;
import com.stu.edu.vn.backend.auth.dto.StartPasswordRecoveryRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyPasswordRecoveryRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyPasswordRecoveryResponse;
import com.stu.edu.vn.backend.auth.service.PasswordRecoveryService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public API Password Recovery; mọi token chỉ nhận qua X-Auth-Flow-Token. */
@RestController
@RequestMapping("/api/v1/auth/password-recovery")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final PasswordRecoveryService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PasswordRecoveryChallengeResponse>> start(
            @Valid @RequestBody StartPasswordRecoveryRequest request
    ) {
        return noStore(HttpStatus.ACCEPTED, "Nếu tài khoản đủ điều kiện, mã xác minh sẽ được gửi", service.start(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerifyPasswordRecoveryResponse>> verify(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token,
            @Valid @RequestBody VerifyPasswordRecoveryRequest request
    ) {
        return noStore(HttpStatus.OK, "Xác minh mã thành công", service.verify(token, request));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<PasswordRecoveryChallengeResponse>> resend(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token
    ) {
        return noStore(HttpStatus.OK, "Nếu tài khoản đủ điều kiện, mã xác minh mới sẽ được gửi", service.resend(token));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<CompletePasswordRecoveryResponse>> complete(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token,
            @Valid @RequestBody CompletePasswordRecoveryRequest request
    ) {
        return noStore(HttpStatus.OK, "Đặt lại mật khẩu thành công", service.complete(token, request));
    }

    private <T> ResponseEntity<ApiResponse<T>> noStore(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success(message, data));
    }
}
