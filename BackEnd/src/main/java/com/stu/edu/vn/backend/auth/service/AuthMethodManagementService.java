package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.auth.dto.AuthMethodResponse;
import com.stu.edu.vn.backend.auth.dto.AuthMethodsResponse;
import com.stu.edu.vn.backend.auth.dto.FacebookAuthRequest;
import com.stu.edu.vn.backend.auth.dto.GoogleAuthRequest;
import com.stu.edu.vn.backend.auth.dto.LinkChallengeResponse;
import com.stu.edu.vn.backend.auth.enums.AuthMethodLinkPurpose;
import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.support.NormalizedEmail;

public interface AuthMethodManagementService {
    AuthMethodsResponse list();
    LinkChallengeResponse start(NormalizedEmail identifier, AuthMethodLinkPurpose purpose);
    LinkChallengeResponse resend(String flowToken, AuthMethodLinkPurpose purpose);
    AuthMethodResponse verify(String flowToken, String code, AuthMethodLinkPurpose purpose);
    AuthMethodResponse linkGoogle(GoogleAuthRequest request);
    AuthMethodResponse linkFacebook(FacebookAuthRequest request);
    void unlink(AuthMethod method, String reauthenticationToken);
}
