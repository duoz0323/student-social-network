package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.CancelRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.CancelRegistrationResponse;
import com.stu.edu.vn.backend.auth.dto.RegistrationStatusResponse;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationRequest;
import com.stu.edu.vn.backend.auth.dto.ResendRegistrationResponse;

/** Quản lý resend, status và cancel của pending registration đã được tạo. */
public interface RegistrationLifecycleService {

    ResendRegistrationResponse resend(ResendRegistrationRequest request);

    RegistrationStatusResponse status(String registrationFlowToken);

    CancelRegistrationResponse cancel(CancelRegistrationRequest request);
}
