package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthResponse;

public interface FacebookAuthService {
    FacebookAuthResponse authenticate(FacebookAuthRequest request, String registrationFlowToken, String ipAddress);

    default FacebookAuthResponse authenticate(FacebookAuthRequest request, String ipAddress) {
        return authenticate(request, null, ipAddress);
    }
}
