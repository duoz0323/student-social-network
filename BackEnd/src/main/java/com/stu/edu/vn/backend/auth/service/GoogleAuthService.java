package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthResponse;

/** Use case Google Auth xác minh provider ngoài transaction rồi ủy quyền xử lý database. */
public interface GoogleAuthService {
    GoogleAuthResponse authenticate(GoogleAuthRequest request, String registrationFlowToken, String ipAddress);

    default GoogleAuthResponse authenticate(GoogleAuthRequest request, String ipAddress) {
        return authenticate(request, null, ipAddress);
    }
}
