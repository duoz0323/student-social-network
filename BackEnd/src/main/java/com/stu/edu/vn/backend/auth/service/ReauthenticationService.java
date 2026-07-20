package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.ReauthenticationRequest;
import com.stu.edu.vn.backend.auth.dto.ReauthenticationResponse;

/** Use case phát hành reauthentication token cho chính user đang đăng nhập. */
public interface ReauthenticationService {

    ReauthenticationResponse reauthenticate(ReauthenticationRequest request);
}
