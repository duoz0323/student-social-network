package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.ChangePasswordRequest;
import com.stu.edu.vn.backend.auth.dto.PasswordMutationResponse;
import com.stu.edu.vn.backend.auth.dto.SetPasswordRequest;

public interface PasswordManagementService {
    PasswordMutationResponse setPassword(String reauthenticationToken, SetPasswordRequest request);
    PasswordMutationResponse changePassword(ChangePasswordRequest request);
}
