package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.LoginRequest;
import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.auth.dto.LogoutRequest;
import com.stu.edu.vn.backend.auth.dto.LogoutResponse;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenRequest;
import com.stu.edu.vn.backend.auth.dto.RefreshTokenResponse;
import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;

/**
 * Service Auth xử lý nghiệp vụ xác thực, không để Controller chứa logic đăng ký.
 */
public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String ipAddress);

    RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request);

    LogoutResponse logout(LogoutRequest request);
}
