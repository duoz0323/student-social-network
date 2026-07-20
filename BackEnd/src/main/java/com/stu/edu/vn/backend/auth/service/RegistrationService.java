package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.RegisterRequest;
import com.stu.edu.vn.backend.auth.dto.RegisterResponse;

/** Use case bắt đầu đăng ký local, chưa tạo user hoặc phiên đăng nhập. */
public interface RegistrationService {

    RegisterResponse start(RegisterRequest request);
}
