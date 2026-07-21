package com.stu.edu.vn.backend.auth.controller;

import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;
import com.stu.edu.vn.backend.auth.service.AuthService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.util.ClientIpAddressResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller Auth chỉ nhận request đăng ký và ủy quyền toàn bộ nghiệp vụ cho AuthService.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final ClientIpAddressResolver clientIpAddressResolver;

    public AuthController(
            AuthService authService,
            ClientIpAddressResolver clientIpAddressResolver
    ) {
        this.authService = authService;
        this.clientIpAddressResolver = clientIpAddressResolver;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        // Controller chỉ lấy IP từ request hạ tầng, toàn bộ nghiệp vụ đăng nhập nằm trong Service.
        String ipAddress = clientIpAddressResolver.resolve(servletRequest);
        LoginResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        // Controller chỉ nhận request, Service chịu trách nhiệm kiểm tra token và cấp Access Token mới.
        RefreshTokenResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(ApiResponse.success("Làm mới Access Token thành công", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(@Valid @RequestBody LogoutRequest request) {
        // Endpoint logout yêu cầu Access Token hợp lệ qua SecurityConfig, Service chỉ xử lý thu hồi Refresh Token.
        LogoutResponse response = authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", response));
    }
}
