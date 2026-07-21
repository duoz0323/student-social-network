package com.stu.edu.vn.backend.auth.controller;

import com.stu.edu.vn.backend.auth.dto.CancelRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthResponse;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictRequest;
import com.stu.edu.vn.backend.auth.dto.ResolveSocialConflictResponse;
import com.stu.edu.vn.backend.auth.dto.RegistrationStatusResponse;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.VerifyRegistrationResponse;
import com.stu.edu.vn.backend.auth.service.AuthService;
import com.stu.edu.vn.backend.auth.service.GoogleAuthService;
import com.stu.edu.vn.backend.auth.service.FacebookAuthService;
import com.stu.edu.vn.backend.auth.service.RegistrationLifecycleService;
import com.stu.edu.vn.backend.auth.service.RegistrationService;
import com.stu.edu.vn.backend.auth.service.RegistrationVerificationService;
import com.stu.edu.vn.backend.auth.service.SocialConflictResolutionService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller Auth chỉ tiếp nhận HTTP và ủy quyền nghiệp vụ cho Service. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final FacebookAuthService facebookAuthService;
    private final RegistrationService registrationService;
    private final RegistrationVerificationService registrationVerificationService;
    private final RegistrationLifecycleService registrationLifecycleService;
    private final SocialConflictResolutionService socialConflictResolutionService;
    private final ClientIpAddressResolver clientIpAddressResolver;

    @PostMapping("/registrations")
    public ResponseEntity<ApiResponse<RegisterResponse>> startRegistration(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = registrationService.start(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đã tạo đăng ký chờ xác minh OTP", response));
    }

    @PostMapping("/registrations/verify")
    public ResponseEntity<ApiResponse<VerifyRegistrationResponse>> verifyRegistration(
            @Valid @RequestBody VerifyRegistrationRequest request,
            HttpServletRequest servletRequest
    ) {
        VerifyRegistrationResponse response = registrationVerificationService.verify(
                request,
                clientIpAddressResolver.resolve(servletRequest)
        );
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Xác minh đăng ký thành công", response));
    }

    @PostMapping("/registrations/resend")
    public ResponseEntity<ApiResponse<ResendRegistrationResponse>> resendRegistration(
            @Valid @RequestBody ResendRegistrationRequest request
    ) {
        ResendRegistrationResponse response = registrationLifecycleService.resend(request);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đã gửi lại OTP", response));
    }

    @GetMapping("/registrations/status")
    public ResponseEntity<ApiResponse<RegistrationStatusResponse>> registrationStatus(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String registrationFlowToken
    ) {
        RegistrationStatusResponse response = registrationLifecycleService.status(registrationFlowToken);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đã đọc trạng thái đăng ký", response));
    }

    @PostMapping("/registrations/cancel")
    public ResponseEntity<ApiResponse<CancelRegistrationResponse>> cancelRegistration(
            @Valid @RequestBody CancelRegistrationRequest request
    ) {
        CancelRegistrationResponse response = registrationLifecycleService.cancel(request);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success(response.message(), response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = clientIpAddressResolver.resolve(servletRequest);
        LoginResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<ApiResponse<GoogleAuthResponse>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request,
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String registrationFlowToken,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = clientIpAddressResolver.resolve(servletRequest);
        GoogleAuthResponse response = registrationFlowToken == null
                ? googleAuthService.authenticate(request, ipAddress)
                : googleAuthService.authenticate(request, registrationFlowToken, ipAddress);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đăng nhập Google thành công", response));
    }

    @PostMapping("/oauth/facebook")
    public ResponseEntity<ApiResponse<FacebookAuthResponse>> facebookAuth(
            @Valid @RequestBody FacebookAuthRequest request,
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String registrationFlowToken,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = clientIpAddressResolver.resolve(servletRequest);
        FacebookAuthResponse response = registrationFlowToken == null
                ? facebookAuthService.authenticate(request, ipAddress)
                : facebookAuthService.authenticate(request, registrationFlowToken, ipAddress);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đăng nhập Facebook thành công", response));
    }

    @PostMapping("/registrations/resolve-social-conflict")
    public ResponseEntity<ApiResponse<ResolveSocialConflictResponse>> resolveSocialConflict(
            @RequestHeader(name = "X-Auth-Flow-Token", required = false) String challengeToken,
            @Valid @RequestBody ResolveSocialConflictRequest request,
            HttpServletRequest servletRequest
    ) {
        ResolveSocialConflictResponse response = socialConflictResolutionService.resolve(
                challengeToken, request, clientIpAddressResolver.resolve(servletRequest));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đã xử lý lựa chọn đăng ký social", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        RefreshTokenResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Làm mới Access Token thành công", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(@Valid @RequestBody LogoutRequest request) {
        LogoutResponse response = authService.logout(request);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Đăng xuất thành công", response));
    }
}
