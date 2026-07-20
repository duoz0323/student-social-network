package com.stu.edu.vn.backend.auth.controller;

import com.stu.edu.vn.backend.auth.dto.AuthMethodResponse;
import com.stu.edu.vn.backend.auth.dto.AuthMethodsResponse;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.dto.LinkChallengeResponse;
import com.stu.edu.vn.backend.auth.dto.LinkEmailRequest;
import com.stu.edu.vn.backend.auth.dto.LinkOtpRequest;
import com.stu.edu.vn.backend.auth.dto.LinkPhoneRequest;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.service.AuthMethodManagementService;
import com.stu.edu.vn.backend.auth.support.IdentifierNormalizer;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP adapter cho auth method của chính user trong JWT; không nhận userId từ client. */
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
                service.start(IdentifierNormalizer.normalize(request.email()), AuthMethodLinkPurpose.LINK_EMAIL)));
    }

    @PostMapping("/phone")
    public ResponseEntity<ApiResponse<LinkChallengeResponse>> startPhone(@Valid @RequestBody LinkPhoneRequest request) {
        return noStore(HttpStatus.ACCEPTED, ApiResponse.success("Đã gửi OTP xác minh số điện thoại",
                service.start(IdentifierNormalizer.normalize(request.phoneNumber()), AuthMethodLinkPurpose.LINK_PHONE)));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<AuthMethodResponse>> verifyEmail(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token,
            @Valid @RequestBody LinkOtpRequest request) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã liên kết email",
                service.verify(token, request.code(), AuthMethodLinkPurpose.LINK_EMAIL)));
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<ApiResponse<AuthMethodResponse>> verifyPhone(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token,
            @Valid @RequestBody LinkOtpRequest request) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã liên kết số điện thoại",
                service.verify(token, request.code(), AuthMethodLinkPurpose.LINK_PHONE)));
    }

    @PostMapping("/email/resend")
    public ResponseEntity<ApiResponse<LinkChallengeResponse>> resendEmail(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã gửi lại OTP email",
                service.resend(token, AuthMethodLinkPurpose.LINK_EMAIL)));
    }

    @PostMapping("/phone/resend")
    public ResponseEntity<ApiResponse<LinkChallengeResponse>> resendPhone(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String token) {
        return noStore(HttpStatus.OK, ApiResponse.success("Đã gửi lại OTP số điện thoại",
                service.resend(token, AuthMethodLinkPurpose.LINK_PHONE)));
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
    public ResponseEntity<Void> unlink(
            @PathVariable AuthMethod provider,
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String reauthenticationToken) {
        service.unlink(provider, reauthenticationToken);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").build();
    }

    private <T> ResponseEntity<ApiResponse<T>> noStore(HttpStatus status, ApiResponse<T> body) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").body(body);
    }
}
