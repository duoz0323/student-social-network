package com.stu.edu.vn.backend.auth.facebook;

/** Biên xác minh duy nhất mà nghiệp vụ Auth được phép dùng với Facebook token. */
public interface FacebookAccessTokenVerifier {
    VerifiedFacebookIdentity verify(String accessToken);
}
