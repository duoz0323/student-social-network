package com.stu.edu.vn.backend.auth.controller;

import com.stu.edu.vn.backend.auth.dto.*;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.service.AuthMethodManagementService;
import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** API quản lý EMAIL, GOOGLE và FACEBOOK của người dùng hiện tại. */
@RestController
@RequestMapping("/api/v1/users/me/auth-providers")
public class AuthMethodController {
    private final AuthMethodManagementService service;

    public AuthMethodController(AuthMethodManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AuthMethodsResponse>> list() {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã đọc phương thức đăng nhập", service.list()));
    }

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<LinkChallengeResponse>> startEmail(@Valid @RequestBody LinkEmailRequest request) {
        return noStore(HttpStatus.ACCEPTED, ApiResponse.success("Đã gửi OTP xác minh email",
                service.start(EmailNormalizer.normalize(request.email()), AuthMethodLinkPurpose.LINK_EMAIL)));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<AuthMethodResponse>> verifyEmail(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token,
            @Valid @RequestBody LinkOtpRequest request) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã liên kết email",
                service.verify(token, request.code(), AuthMethodLinkPurpose.LINK_EMAIL)));
    }

    @PostMapping("/email/resend")
    public ResponseEntity<ApiResponse<LinkChallengeResponse>> resendEmail(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã gửi lại OTP email",
                service.resend(token, AuthMethodLinkPurpose.LINK_EMAIL)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthMethodResponse>> linkGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã liên kết Google", service.linkGoogle(request)));
    }

    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse<AuthMethodResponse>> linkFacebook(@Valid @RequestBody FacebookAuthRequest request) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã liên kết Facebook", service.linkFacebook(request)));
    }

    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> unlink(@PathVariable AuthMethod provider,
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token) {
        service.unlink(provider, token);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").build();
    }

    private <T> ResponseEntity<ApiResponse<T>> noStore(HttpStatus status, ApiResponse<T> body) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").body(body);
    }
}
